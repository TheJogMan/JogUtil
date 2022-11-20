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
		addArgumentList("See information about a specific component.");
		addArgument(1, ComponentArgument.class, "Command/Category", new Object[] {parent});
		addArgumentList("See information about a specific variation of a commands arguments.");
		addArgument(2, ComponentArgument.class, "Command", new Object[] {parent, Command.class});
		addArgument(2, IntegerValue.class, "Variant");
	}
	
	@Override
	protected void execute(AdaptiveInterpretation result, Executor executor)
	{
	
	}
	
	@CommandExecutor
	public void category(Executor executor)
	{
		viewCategory(parent(), executor);
	}
	
	@CommandExecutor
	public void component(CommandComponent component, Executor executor)
	{
		if (component instanceof Category)
			viewCategory((Category)component, executor);
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
	
	void viewCommand(Command command, Executor executor)
	{
		RichStringBuilder builder = RichStringBuilder.start();
		builder.append("Description:", Style.create().color(RichColor.ORANGE)).newLine().append(' ').append(command.description()).newLine();
		builder.append("Variants:", Style.create().color(RichColor.ORANGE));
		for (int index = 0; index < command.argumentListCount(); index++)
			builder.newLine().append((index + 1) + ": ").append(command.usage(index), Style.create().color(RichColor.AQUA));
		builder.newLine().style(Style.create().color(RichColor.GREEN)).append("Use ")
			   .append(command.parent().helpCommand.fullName(2, true), Style.create().color(RichColor.LIME).highlighted(true))
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
	
	static void viewCategory(Category category, Executor executor)
	{
		ArrayList<Command> commands = new ArrayList<>();
		ArrayList<Category> categories = new ArrayList<>();
		for (CommandComponent component : category.getContext(executor))
		{
			if (component.canExecute(executor).success())
			{
				if (component instanceof Command)
					commands.add((Command)component);
				else if (component instanceof Category)
					categories.add((Category)component);
			}
		}
		RichStringBuilder builder = RichStringBuilder.start();
		builder.append("Description:", Style.create().color(RichColor.ORANGE));
		builder.newLine().append(" ");
		builder.append(category.description());
		builder.newLine().append("Sub-Categories:", Style.create().color(RichColor.ORANGE));
		builder.newLine().append(" ");
		if (categories.size() == 0)
			builder.append("None.", Style.create().color(RichColor.AQUA).highlighted(true));
		else
		{
			for (Iterator<Category> iterator = categories.iterator(); iterator.hasNext();)
			{
				builder.append(iterator.next().name(), Style.create().color(RichColor.AQUA).highlighted(true));
				if (iterator.hasNext())
					builder.append(", ");
			}
		}
		builder.newLine().append("Commands:", Style.create().color(RichColor.ORANGE));
		builder.newLine().append(" ");
		if (commands.size() == 0)
			builder.append("None.", Style.create().color(RichColor.AQUA).highlighted(true));
		else
		{
			for (Iterator<Command> iterator = commands.iterator(); iterator.hasNext();)
			{
				builder.append(iterator.next().name(), Style.create().color(RichColor.AQUA).highlighted(true));
				if (iterator.hasNext())
					builder.append(", ");
			}
		}
		builder.newLine().append("Use ", Style.create().color(RichColor.GREEN))
			   .append(category.helpCommand.fullName(1, true), Style.create().color(RichColor.LIME).highlighted(true))
			   .append(" to learn more.", Style.create().color(RichColor.GREEN));
		executor.respond(builder.build());
	}
}