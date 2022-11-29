package jogUtil.commander.command.commands;

import jogUtil.commander.*;
import jogUtil.commander.argument.*;
import jogUtil.commander.argument.arguments.*;
import jogUtil.commander.command.*;
import jogUtil.data.values.*;
import jogUtil.richText.*;

import java.util.*;

public class HelpCommand extends Command
{
	public HelpCommand(Category parent)
	{
		super(parent, "Help", "Provides a simple means of learning about available commands.");
		
		setDefaultListDescription("See a list of available commands and sub-categories.");
		addArgumentList("See a specific page of available commands and sub-categories.");
		addArgument(1, IntegerValue.class, "Page");
		addArgumentList("See information about a specific component.");
		addArgument(2, ComponentArgument.class, "Command/Category", new Object[] {parent});
		addArgumentList("See information about a specific variation of a commands arguments.");
		addArgument(3, ComponentArgument.class, "Command", new Object[] {parent, Command.class});
		addArgument(3, IntegerValue.class, "Variant");
	}
	
	@Override
	protected void execute(AdaptiveInterpretation result, Executor executor)
	{
	
	}
	
	@CommandExecutor
	public void category(Executor executor)
	{
		viewCategory(parent(), executor, 1);
	}
	
	@CommandExecutor
	public void categoryPage(Integer page, Executor executor)
	{
		viewCategory(parent(), executor, page);
	}
	
	@CommandExecutor
	public void component(CommandComponent component, Executor executor)
	{
		if (component instanceof Category)
			viewCategory((Category)component, executor, 1);
		else if (component instanceof Command)
		{
			if (((Command)component).argumentListCount() == 1)
				viewCommandVariant((Command)component, 0, executor, true);
			else
				viewCommand((Command)component, executor);
		}
	}
	
	@CommandExecutor
	public void variant(CommandComponent component, Integer variant, Executor executor)
	{
		Command command = (Command)component;
		variant--;
		if (variant >= 0 && variant < command.argumentListCount())
			viewCommandVariant(command, variant, executor, false);
		else
			executor.respond("That command has " + command.argumentListCount() + " variants, " + (variant + 1) + " is not in the range of 1-" + command.argumentListCount());
	}
	
	static void describeComponent(CommandComponent component, RichStringBuilder builder)
	{
		builder.append("Description:", Style.create().color(RichColor.ORANGE)).newLine().append(' ').append(component.description());
		if (component.aliasCount() > 0)
		{
			builder.newLine().append("Aliases:", Style.create().color(RichColor.ORANGE)).newLine().append(' ');
			for (Iterator<String> aliasIterator = component.aliasIterator(); aliasIterator.hasNext();)
			{
				builder.append(aliasIterator.next(), Style.create().color(RichColor.AQUA).highlighted(true));
				if (aliasIterator.hasNext())
					builder.append(", ", Style.create().color(RichColor.ORANGE));
			}
		}
	}
	
	void viewCommand(Command command, Executor executor)
	{
		RichStringBuilder builder = RichStringBuilder.start();
		describeComponent(command, builder);
		builder.newLine().append("Variants:", Style.create().color(RichColor.ORANGE));
		for (int index = 0; index < command.argumentListCount(); index++)
			builder.newLine().append((index + 1) + ": ").append(command.usage(index), Style.create().color(RichColor.AQUA));
		builder.newLine().style(Style.create().color(RichColor.GREEN)).append("Use ")
			   .append(command.parent().helpCommand.fullName(3, true), Style.create().color(RichColor.LIME).highlighted(true))
			   .append(" to learn about a specific variant.");
		executor.respond(builder.build());
	}
	
	static void viewCommandVariant(Command command, int variant, Executor executor, boolean useCommandDescription)
	{
		RichStringBuilder builder = RichStringBuilder.start();
		AdaptiveArgumentList.ArgumentListEntry argumentList = command.getArgumentList(variant);
		builder.append("Description:", Style.create().color(RichColor.ORANGE)).newLine().append(' ').append((useCommandDescription ? command.description() : argumentList.description()));
		builder.newLine().append("Usage:", Style.create().color(RichColor.ORANGE)).newLine();
		builder.append(' ').append(command.usage(variant), Style.create().color(RichColor.AQUA));
		builder.newLine().append("Arguments:", Style.create().color(RichColor.ORANGE));
		if (argumentList.list().argumentCount() == 0)
			builder.newLine().append("None.", Style.create().highlighted(true));
		else
		{
			for (int index = 0; index < argumentList.list().argumentCount(); index++)
			{
				ArgumentList.ArgumentEntry argument = argumentList.list().getArgument(index);
				builder.newLine().append(' ').append(argument.name(), Style.create().highlighted(true).color(RichColor.AQUA)).append(" - ").append(argument.description());
			}
		}
		executor.respond(builder.build());
	}
	
	static void viewCategory(Category category, Executor executor, int pageNumber)
	{
		Pages pages = new Pages(category.getContext(executor));
		if (pageNumber < 1 || pageNumber > pages.pageCount())
		{
			executor.respond("Invalid page number, must be in range of 1-" + pages.pageCount());
			return;
		}
		Pages.Page page = pages.get(pageNumber - 1);
		Pages.PageList<Command> commands = page.commands();
		Pages.PageList<Category> categories = page.categories();
		
		Style orange = Style.create().color(RichColor.ORANGE);
		Style green = Style.create().color(RichColor.GREEN);
		Style aqua = Style.create().color(RichColor.AQUA).highlighted(true);
		Style lime = Style.create().color(RichColor.LIME).highlighted(true);
		
		RichStringBuilder builder = RichStringBuilder.start();
		describeComponent(category, builder);
		builder.newLine().append("Sub-Categories:", orange);
		builder.newLine().append(" ");
		if (categories.size() == 0)
			builder.append("None.", aqua);
		else
		{
			for (Iterator<Category> iterator = categories.iterator(); iterator.hasNext();)
			{
				builder.append(iterator.next().name(), aqua);
				if (iterator.hasNext())
					builder.append(", ", orange);
			}
		}
		builder.newLine().append("Commands:", orange);
		builder.newLine().append(" ");
		if (commands.size() == 0)
			builder.append("None.", aqua);
		else
		{
			for (Iterator<Command> iterator = commands.iterator(); iterator.hasNext();)
			{
				builder.append(iterator.next().name(), aqua);
				if (iterator.hasNext())
					builder.append(", ", orange);
			}
		}
		builder.newLine().append("Use ", green)
			   .append(category.helpCommand.fullName(2, true), lime)
			   .append(" to learn more.", green);
		if (pages.pageCount() > 1)
		{
			builder.newLine().append("Page: ", orange).append(pageNumber + "", aqua).append(" of ", orange).append(pages.pageCount() + "", aqua).append(".", orange);
			builder.newLine().append("Use ", green)
				   .append(category.helpCommand.fullName(1, true), lime)
				   .append(" to view a different page.", green);
		}
		executor.respond(builder.build());
	}
	
	public static class Pages implements Iterable<Pages.Page>
	{
		final int lineLengthLimit;
		final ArrayList<Page> pages = new ArrayList<>();
		final Category.Context context;
		final boolean hideInexecutable;
		
		public Pages(Category.Context context, int lineLengthLimit, boolean hideInexecutable)
		{
			this.context = context;
			this.lineLengthLimit = lineLengthLimit;
			this.hideInexecutable = hideInexecutable;
			pages.add(new Page());
			context.forEach(this::add);
		}
		
		public Pages(Category.Context context, int lineLengthLimit)
		{
			this(context, lineLengthLimit, true);
		}
		
		public Pages(Category.Context context)
		{
			this(context, 190);
		}
		
		public Pages(Category.Context context, boolean hideInexecutable)
		{
			this(context, 190, hideInexecutable);
		}
		
		void add(CommandComponent component)
		{
			if (!hideInexecutable || !component.canExecute(context.contextSource()).success())
				return;
			
			for (Page page : pages)
			{
				if (page.canFit(component))
				{
					page.add(component);
					return;
				}
			}
			pages.add(new Page(component));
		}
		
		public int pageCount()
		{
			return pages.size();
		}
		
		public Page get(int index)
		{
			return pages.get(index);
		}
		
		public Category.Context context()
		{
			return context;
		}
		
		@Override
		public Iterator<Page> iterator()
		{
			return pages.iterator();
		}
		
		public class Page
		{
			final PageList<Command> commands = new PageList<>();
			final PageList<Category> categories = new PageList<>();
			
			Page(CommandComponent firstComponent)
			{
				add(firstComponent);
			}
			
			Page()
			{
			
			}
			
			void add(CommandComponent component)
			{
				if (component instanceof Command command)
					commands.add(command);
				else if (component instanceof Category category)
					categories.add(category);
			}
			
			boolean canFit(CommandComponent component)
			{
				if (component instanceof Command command)
					return commands.canFit(command);
				else if (component instanceof Category category)
					return categories.canFit(category);
				else
					return false;
			}
			
			public PageList<Command> commands()
			{
				return commands;
			}
			
			public PageList<Category> categories()
			{
				return categories;
			}
		}
		
		public class PageList<Type extends CommandComponent> implements Iterable<Type>
		{
			final ArrayList<Type> components = new ArrayList<>();
			int length = 0;
			
			int newLength(Type component)
			{
				int newLength = length + component.name().length();
				if (components.size() > 0)
					newLength += 2;
				return newLength;
			}
			
			boolean canFit(Type component)
			{
				return newLength(component) <= lineLengthLimit;
			}
			
			void add(Type component)
			{
				length = newLength(component);
				components.add(component);
			}
			
			@Override
			public Iterator<Type> iterator()
			{
				return components.iterator();
			}
			
			public int size()
			{
				return components.size();
			}
			
			public Type get(int index)
			{
				return components.get(index);
			}
		}
	}
}