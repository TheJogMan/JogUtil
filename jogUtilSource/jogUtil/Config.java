package jogUtil;

import jogUtil.commander.*;
import jogUtil.commander.argument.*;
import jogUtil.commander.command.*;
import jogUtil.data.*;
import jogUtil.data.values.*;
import jogUtil.indexable.*;
import jogUtil.richText.*;

import java.io.*;
import java.util.*;

public class Config
{
	private Data configData;
	private File file = null;
	private final ArrayList<Setting<?>> settings = new ArrayList<>();
	private String name;
	private RichString description;
	
	private void init(String name, RichString description)
	{
		this.name = name;
		this.description = description;
		load();
	}
	
	public Config(String name, RichString description)
	{
		init(name, description);
	}
	
	public Config(String name, String description)
	{
		this(name, new RichString(description));
	}
	
	public Config(File file, String name, RichString description)
	{
		this.file = file;
		init(name, description);
	}
	
	public Config(File file, String name, String description)
	{
		this(file, name, new RichString(description));
	}
	
	public final IOException load()
	{
		try
		{
			if (file != null && file.exists() && file.canRead())
			{
				Consumer.ConsumptionResult<Value<?, Data>, Character> result = DataValue.getCharacterConsumer().consume(new IndexableReader(new FileReader(file)).iterator());
				if (result.success())
				{
					configData = (Data)result.value().get();
					settings.forEach(Setting::catchValue);
					return null;
				}
			}
			
			//if the file could not be read, or the data could not be parsed, then we will end up here, where we can initialize default settings
			configData = new Data();
			settings.forEach(setting -> configData.put(setting.name, setting.value));
			save();
			
			return null;
		}
		catch (IOException e)
		{
			e.printStackTrace();
			return e;
		}
	}
	
	public final IOException save()
	{
		try
		{
			if (file != null)
			{
				FileWriter stream = new FileWriter(file);
				stream.write(configData.toString());
				stream.close();
			}
			return null;
		}
		catch (IOException e)
		{
			return e;
		}
	}
	
	public final void reset()
	{
		if (file != null)
			file.delete();
		load();
	}
	
	public final Setting<?> getSetting(String name)
	{
		for (Setting<?> setting : settings)
		{
			if (setting.name.equals(name))
				return setting;
		}
		return null;
	}
	
	public final ArrayList<Setting<?>> settings()
	{
		return (ArrayList<Setting<?>>)settings.clone();
	}
	
	public <Type> Setting<Type> createSetting(String name, TypeRegistry.RegisteredType type, Value<Type, Type> defaultValue, String description)
	{
		return createSetting(name, type, defaultValue, new RichString(description), new Object[0]);
	}
	
	public <Type> Setting<Type> createSetting(String name, TypeRegistry.RegisteredType type, Value<Type, Type> defaultValue, String description, Object[] argumentData)
	{
		return createSetting(name, type, defaultValue, new RichString(description));
	}
	
	public <Type> Setting<Type> createSetting(String name, TypeRegistry.RegisteredType type, Value<Type, Type> defaultValue, RichString description)
	{
		return createSetting(name, type, defaultValue, description, new Object[0]);
	}
	
	public <Type> Setting<Type> createSetting(String name, TypeRegistry.RegisteredType type, Value<Type, Type> defaultValue, RichString description, Object[] argumentData)
	{
		if (name.indexOf(' ') != -1)
			throw new IllegalArgumentException("Setting name can not contain a space");
		settings.forEach(setting ->
		{
			if (setting.name.equals(name))
				throw new IllegalArgumentException("Two settings can not have the same name.");
		});
		return new Setting<>(name, type, defaultValue, description, argumentData);
	}
	
	public static interface ChangeListener<Type>
	{
		public void change(Type oldValue, Type newValue);
	}
	
	public final class Setting<Type>
	{
		private class SaveListener implements Value.ValueChangeListener<Type>
		{
			@Override
			public void change(Type oldValue, Type newValue)
			{
				save();
				listeners.forEach(listener -> listener.change(oldValue, newValue));
			}
		}
		
		private final String name;
		private Value<Type, Type> value;
		private final Value<Type, Type> defaultValue;
		private final TypeRegistry.RegisteredType type;
		private final SaveListener listener = new SaveListener();
		private final ArrayList<ChangeListener<Type>> listeners = new ArrayList<>();
		private final RichString description;
		private final Object[] argumentData;
		
		Setting(String name, TypeRegistry.RegisteredType type, Value<Type, Type> defaultValue, RichString description, Object[] argumentData)
		{
			this.name = name;
			this.defaultValue = defaultValue;
			this.type = type;
			this.description = description;
			this.argumentData = argumentData;
			
			catchValue(); //capture a reference to the value within the data
			settings.add(this);
			save();
		}
		
		public void addListener(ChangeListener<Type> listener)
		{
			listeners.add(listener);
		}
		
		public void removeListener(ChangeListener<Type> listener)
		{
			listeners.remove(listener);
		}
		
		private Value<Type, Type> defaultValue()
		{
			return defaultValue.copy();
		}
		
		/*
		 * capture a reference to the value within the data
		 */
		private void catchValue()
		{
			if (value != null) //if we already have a reference to a value object, we need to detach our change listener from it
				value.removeChangeListener(listener);
			
			//get a reference to the value object, also creates the value object if it doesn't already exist in the data
			//and then attach our change listener to it
			
			
			Value<Type, Type> defaultVal = defaultValue();
			Value<?, ?> ret = configData.get(name, defaultVal);
			value = (Value<Type, Type>)ret;
			
			
			
			
			//value = (Value<Type, Type>)configData.get(name, defaultValue());
			value.addChangeListener(listener);
		}
		
		public RichString description()
		{
			return description;
		}
		
		public Type get()
		{
			return value.get();
		}
		
		public void set(Type value)
		{
			this.value.set(value);
		}
		
		public void reset()
		{
			value.set(defaultValue.get());
		}
		
		public void addCommands(Category parent)
		{
			Category settingCategory = new Category(parent, name, description);
			new Set(settingCategory);
			new Reset(settingCategory);
			new Get(settingCategory, this);
		}
		
		private class Set extends Command
		{
			private Set(Category parent)
			{
				super(parent, "Set", "Set the value.");
				addArgument(type.typeClass(), argumentData, "New value.");
			}
			
			@Override
			public void execute(AdaptiveInterpretation result, Executor executor)
			{
				Type newValue = (Type)result.value()[0];
				RichStringBuilder builder = new RichStringBuilder();
				builder.append("Changed from " + value.toString() + " to ");
				value.set(newValue);
				builder.append(value.toString() + ".");
				executor.respond(builder.build());
			}
		}
		
		private class Reset extends Command
		{
			private Reset(Category parent)
			{
				super(parent, "Reset", "Reset the value.");
			}
			
			@Override
			public void execute(AdaptiveInterpretation result, Executor executor)
			{
				RichStringBuilder builder = new RichStringBuilder();
				builder.append("Changed from " + value.toString() + " to ");
				value.set(defaultValue.get());
				builder.append(value.toString() + ".");
				executor.respond(builder.build());
			}
		}
		
		private class Get extends Command
		{
			final Setting<Type> setting;
			
			private Get(Category parent, Setting<Type> setting)
			{
				super(parent, "Get", "Gets the current value.");
				this.setting = setting;
			}
			
			@Override
			public void execute(AdaptiveInterpretation result, Executor executor)
			{
				executor.respond("Current value is " + value.toString() + ". Default value is " + setting.defaultValue());
			}
		}
	}
	
	public Category addCommands(Category parent)
	{
		Category configCategory = new Category(parent, name, description);
		settings.forEach(setting -> setting.addCommands(configCategory));
		return configCategory;
	}
}