package jogUtil.data.values;

import jogUtil.*;
import jogUtil.commander.*;
import jogUtil.data.*;
import jogUtil.indexable.*;
import jogUtil.richText.*;

import java.util.*;

public class ListValue<Type extends Value<?, ?>> extends Value<List<Type>, List<Value<?, ?>>>
		implements List<Type>
{
	private final TypeRegistry.RegisteredType type;
	final ArrayList<ListChangeListener<Type>> listeners = new ArrayList<>();
	
	private static void validateType(TypeRegistry.RegisteredType type)
	{
		if (type == null)
			throw new IllegalArgumentException("Type must be registered.");
	}
	
	public ListValue(TypeRegistry.RegisteredType type)
	{
		super(new Object[0]);
		validateType(type);
		this.type = type;
	}
	
	public ListValue(TypeRegistry.RegisteredType type, Object[] initData)
	{
		super(initData);
		validateType(type);
		this.type = type;
	}
	
	public ListValue(TypeRegistry.RegisteredType type, List<Type> value)
	{
		this(type);
		set(value);
	}
	
	public static <Type extends Value<?, ?>> ListValue<Type> create(TypeRegistry.RegisteredType type, List<Type> value)
	{
		return new ListValue<>(type, value);
	}
	
	@Override
	public String defaultName()
	{
		return type.name() + "List";
	}
	
	@Override
	public List<String> argumentCompletions(Indexer<Character> source, Executor executor)
	{
		return null;
	}
	
	@Override
	public List<Type> emptyValue()
	{
		return new ArrayList<>();
	}
	
	@Override
	public String asString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append(StringValue.pack(type.name())).append("[\r\n");
		for (int valueIndex = 0; valueIndex < size(); valueIndex++)
		{
			builder.append('\t');
			Type value = get(valueIndex);
			String string = value.toString();
			for (int index = 0; index < string.length(); index++)
			{
				char ch = string.charAt(index);
				builder.append(ch);
				if (ch == '\n')
					builder.append('\t');
			}
			if (valueIndex < size() - 1)
				builder.append(',');
			builder.append("\r\n");
		}
		builder.append(']');
		return builder.toString();
	}
	
	@Override
	public byte[] asBytes()
	{
		ByteArrayBuilder builder = new ByteArrayBuilder();
		builder.add(type.name());
		builder.add(size());
		for (Type value : this)
			builder.add(value.asBytes());
		return builder.toPrimitiveArray();
	}
	
	@Override
	protected Value<List<Type>, List<Value<?, ?>>> makeCopy()
	{
		return create(type, get());
	}
	
	@Override
	protected boolean checkDataEquality(Value<?, ?> value)
	{
		if (value instanceof ListValue && ((ListValue<?>)value).type.equals(type))
		{
			List<Type> otherList = (ListValue<Type>)value;
			if (otherList.size() != size())
				return false;
			for (int index = 0; index < size(); index++)
			{
				if (!get(index).equals(otherList.get(index)))
					return false;
			}
			return true;
		}
		return false;
	}
	
	@Override
	public void initArgument(Object[] args)
	{
	
	}
	
	@TypeRegistry.ByteConsumer
	public static Consumer<Value<?, List<Value<?, ?>>>, Byte> getByteConsumer()
	{
		return (source) ->
		{
			Consumer.ConsumptionResult<Value<?, String>, Byte> typeNameResult =
					StringValue.getByteConsumer().consume(source);
			if (!typeNameResult.success())
				return new Consumer.ConsumptionResult<>(source, RichStringBuilder
						.start("Could not parse type name: ")
						.append(typeNameResult.description()).build());
			String typeName = (String)typeNameResult.value().get();
			TypeRegistry.RegisteredType type = TypeRegistry.get(typeName);
			if (type == null)
				return new Consumer.ConsumptionResult<>(source, "\"" + typeName + "\" is not a registered type.");
			
			Consumer.ConsumptionResult<Value<?, Integer>, Byte> lengthResult =
					IntegerValue.getByteConsumer().consume(source);
			if (!lengthResult.success())
				return new Consumer.ConsumptionResult<>(source, RichStringBuilder
						.start("Could not parse length: ")
						.append(lengthResult.description()).build());
			int length = (int)lengthResult.value().get();
			
			ArrayList<Value<?, ?>> list = new ArrayList<>();
			for (int index = 0; index < length; index++)
			{
				Consumer.ConsumptionResult<Value<?, ?>, Byte> valueResult =
						type.byteConsumer().consume(source);
				if (!valueResult.success())
					return new Consumer.ConsumptionResult<>(source, RichStringBuilder.start("Could not parse value #" + index + " as " + type.name() + ": ")
																					 .append(valueResult.description()).build());
				list.add(valueResult.value());
			}
			return new Consumer.ConsumptionResult<>(create(type, list), source);
		};
	}
	
	@TypeRegistry.CharacterConsumer
	public static Consumer<Value<?, List<Value<?, ?>>>, Character> getCharacterConsumer()
	{
		return (source) ->
		{
			Consumer.ConsumptionResult<Value<?, String>, Character> typeNameResult =
					StringValue.getCharacterConsumer().consume(source);
			if (!typeNameResult.success())
				return new Consumer.ConsumptionResult<>(source, RichStringBuilder.start("Could not parse type name: ").append(typeNameResult.description()).build());
			String typeName = (String)typeNameResult.value().get();
			TypeRegistry.RegisteredType type = TypeRegistry.get(typeName);
			if (type == null)
				return new Consumer.ConsumptionResult<>(source, "\"" + typeName + "\" is not a registered type.");
			
			if (source.next() != '[')
				return new Consumer.ConsumptionResult<>(source, "Expected '[' after type name.");
			source.skip(Data.formattingCharacters);
			
			ArrayList<Value<?, ?>> list = new ArrayList<>();
			boolean expecting = false;
			int index = 0;
			while (!source.atEnd() && source.get() != ']')
			{
				expecting = false;
				
				Consumer.ConsumptionResult<Value<?, ?>, Character> valueResult =
						type.characterConsumer().consume(source);
				if (!valueResult.success())
					return new Consumer.ConsumptionResult<>(source, RichStringBuilder.start("Could not parse value #" + index + " as " + type.name() + ": ")
																					 .append(valueResult.description()).build());
				list.add(valueResult.value());
				
				if (source.get() == ',')
				{
					source.next();
					expecting = true;
				}
				index++;
				source.skip(Data.formattingCharacters);
			}
			if (source.get() != ']')
			{
				if (expecting)
					return new Consumer.ConsumptionResult<>(source, "Value expected after ','");
				else
					return new Consumer.ConsumptionResult<>(source, "Must end with ']'");
			}
			source.next();
			return new Consumer.ConsumptionResult<>(create(type, list), source);
		};
	}
	
	@TypeRegistry.ValidationValues
	public static Value<?, List<Value<?, ?>>>[] validationValues()
	{
		return new ListValue<?>[] {
			makeValidationList(StringValue.class),
			makeValidationList(IntegerValue.class),
			makeValidationList(BooleanValue.class),
			makeValidationList(ByteValue.class),
			makeValidationList(UUIDValue.class)
		};
	}
	
	private static <Type extends Value<?, ?>> ListValue<Type> makeValidationList(Class<Type> typeClass)
	{
		TypeRegistry.RegisteredType type = TypeRegistry.get(typeClass);
		if (type == null)
			throw new RuntimeException(typeClass + " is not a registered type");
		ArrayList<Type> values = new ArrayList<>();
		Collections.addAll(values, (Type[])type.validationValues());
		return create(type, values);
	}
	
	//we need to make a new copy of the list so that we can ensure that there are no external references
	//to it, along the way we can ensure that all values are strictly compatible
	@Override
	public void set(List<Type> list)
	{
		if (list == null)
			throw new IllegalArgumentException("Null values are not supported.");
		if (list.size() == 0)
			super.set(new ArrayList<>());
		else
		{
			ArrayList<Type> newList = new ArrayList<>();
			for (Type value : list)
			{
				if (value == null)
					throw new IllegalArgumentException("Can not add a null value.");
				if (type.typeClass().getGenericSuperclass().equals(value.getClass().getGenericSuperclass()))
					newList.add(value);
				else
					throw new IllegalArgumentException("Value type is not the same as list type. A " + type.typeClass().getGenericSuperclass() + " list can not contain a "
													   + value.getClass().getGenericSuperclass() + " value.");
			}
			super.set(newList);
		}
	}
	
	public static interface ListChangeListener<Type extends Value<?, ?>>
	{
		void cleared();
		void valueAdded(Type value);
		void valueRemoved(Type value);
		void collectionAdded(Collection<? extends Type> collection);
		void collectionRemoved(Collection<? extends Type> collection);
		void valueChanged(int index, Type newValue, Type oldValue);
	}
	
	public ListChangeListener<Type> addListChangeListener(ListChangeListener<Type> listener)
	{
		listeners.add(listener);
		return listener;
	}
	
	public void removeListChangeListener(ListChangeListener<Type> listener)
	{
		listeners.remove(listener);
	}
	
	public Type addValue(Type e)
	{
		if (e == null)
			throw new IllegalArgumentException("Can not add a null value");
		if (get().add(e))
			listeners.forEach(listener -> listener.valueAdded(e));
		return e;
	}
	
	@Override
	public boolean add(Type e)
	{
		if (e == null)
			throw new IllegalArgumentException("Can not add a null value");
		if (get().add(e))
		{
			listeners.forEach(listener -> listener.valueAdded(e));
			return true;
		}
		else
			return false;
	}
	
	@Override
	public void add(int index, Type element)
	{
		if (element == null)
			throw new IllegalArgumentException("Can not add a null value");
		get().add(index, element);
		listeners.forEach(listener -> listener.valueAdded(element));
	}
	
	@Override
	public boolean addAll(Collection<? extends Type> c)
	{
		if (checkForNull(c))
			throw new IllegalArgumentException("Can not add a null value");
		boolean changed = get().addAll(c);
		listeners.forEach(listener -> listener.collectionAdded(c));
		return changed;
	}
	
	@Override
	public boolean addAll(int index, Collection<? extends Type> c)
	{
		if (checkForNull(c))
			throw new IllegalArgumentException("Can not add a null value");
		boolean changed = get().addAll(index, c);
		listeners.forEach(listener -> listener.collectionAdded(c));
		return changed;
	}
	
	@Override
	public void clear()
	{
		get().clear();
		listeners.forEach(ListChangeListener::cleared);
	}
	
	@Override
	public boolean contains(Object o)
	{
		return get().contains(o);
	}
	
	@Override
	public boolean containsAll(Collection<?> c)
	{
		return new HashSet<>(get()).containsAll(c);
	}
	
	@Override
	public Type get(int index)
	{
		return get().get(index);
	}
	
	@Override
	public int indexOf(Object o)
	{
		return get().indexOf(o);
	}
	
	@Override
	public boolean isEmpty()
	{
		return get().isEmpty();
	}
	
	@Override
	public Iterator<Type> iterator()
	{
		return get().iterator();
	}
	
	@Override
	public int lastIndexOf(Object o)
	{
		return get().lastIndexOf(o);
	}
	
	@Override
	public ListIterator<Type> listIterator()
	{
		return get().listIterator();
	}
	
	@Override
	public ListIterator<Type> listIterator(int index)
	{
		return get().listIterator(index);
	}
	
	@Override
	public boolean remove(Object o)
	{
		if (get().remove(o))
		{
			listeners.forEach(listener -> listener.valueRemoved((Type)o));
			return true;
		}
		else
			return false;
	}
	
	@Override
	public Type remove(int index)
	{
		if (index >= 0 && index < size())
		{
			Type entry = get(index);
			remove(entry);
			return entry;
		}
		else
			throw new IndexOutOfBoundsException(index + " is not in range of 0-" + (size() - 1));
	}
	
	@Override
	public boolean removeAll(Collection<?> c)
	{
		boolean changed = get().removeAll(c);
		listeners.forEach(listener -> listener.collectionRemoved((Collection<? extends Type>) c));
		return changed;
	}
	
	@Override
	public boolean retainAll(Collection<?> c)
	{
		ArrayList<Type> removed = new ArrayList<>();
		get().forEach(value ->
		{
			if (!c.contains(value))
				removed.add(value);
		});
		boolean changed = get().retainAll(c);
		listeners.forEach(listener -> listener.collectionRemoved(removed));
		return changed;
	}
	
	@Override
	public Type set(int index, Type element)
	{
		if (element == null)
			throw new IllegalArgumentException("Can not add a null value");
		Type old = get().set(index, element);
		listeners.forEach(listener -> listener.valueChanged(index, element, old));
		return old;
	}
	
	@Override
	public int size()
	{
		return get().size();
	}
	
	@Override
	public List<Type> subList(int fromIndex, int toIndex)
	{
		return get().subList(fromIndex, toIndex);
	}
	
	@Override
	public Object[] toArray()
	{
		return get().toArray();
	}
	
	@Override
	public <T> T[] toArray(T[] a)
	{
		return (T[]) get().toArray(new Value[0]);
	}
	
	private static boolean checkForNull(Collection<?> collection)
	{
		for (Object element : collection)
			if (element == null)
				return true;
		return false;
	}
}