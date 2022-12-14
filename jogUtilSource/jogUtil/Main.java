package jogUtil;

import jogUtil.commander.*;
import jogUtil.commander.argument.*;
import jogUtil.commander.command.Console;
import jogUtil.data.*;
import jogUtil.data.values.*;
import jogUtil.indexable.*;
import jogUtil.richText.*;

import java.io.*;

public class Main
{
	public static void main(String[] args) throws IOException
	{
		Result[] results = TypeRegistry.defaultValueStatus();
		
		Console console = new Console("Test", '/');
		Executor.HeadlessExecutor executor = new Executor.HeadlessExecutor(System.out);
		
		console.interpret(StringValue.indexer("help"), executor);
		System.out.println("#");
		console.interpret(StringValue.indexer("help help"), executor);
		System.out.println("#");
		console.interpret(StringValue.indexer("help help 3"), executor);
		System.out.println("#");
		console.interpret(StringValue.indexer("help he"), executor);
		System.out.println("#");
		console.interpret(StringValue.indexer("he"), executor);
		System.out.println("#");
		console.interpret(StringValue.indexer(""), executor);
		System.out.println("#");
		
		for (Result result : results)
			System.out.println(result.description());
		
		System.out.println(TypeRegistry.register("Test", CompoundArgumentValueTest.class).description());
		
		Data data = new Data();
		data.put("test1", new IntegerValue(1));
		data.put("test2", new IntegerValue(2));
		data.put("test3", new IntegerValue(3));
		data.put("test4", new StringValue("Hello World!"));
		data.put("test5", new BooleanValue(false));
		
		File file = new File("test.txt");
		
		FileWriter writer = new FileWriter(file);
		writer.write(data.toString());
		writer.close();
		Consumer.ConsumptionResult<Data, Character> result = Data.characterConsumer().consume(new IndexableReader(new FileReader(file)).iterator());
		if (!result.success())
			System.out.println("Could not parse file: " + result.description().encode(EncodingType.PLAIN));
		Data data2 = result.value;
		if (!data2.matches(data))
			System.out.println("Parsed data did not match original.");
	}
	
	public static record TestValue(String string, int number)
	{
		public boolean equals(Object object)
		{
			if (object instanceof TestValue otherValue)
			{
				return otherValue.string.compareTo(string) == 0 && otherValue.number == number;
			}
			else
				return false;
		}
	}
	
	public static class CompoundArgumentValueTest extends CompoundArgumentValue<TestValue, TestValue>
	{
		public CompoundArgumentValueTest()
		{
			super();
		}
		
		public CompoundArgumentValueTest(TestValue value)
		{
			super(value);
		}
		
		@Override
		public TestValue emptyValue()
		{
			return new TestValue("Empty", 0);
		}
		
		@Override
		public String asString()
		{
			return StringValue.pack(get().string) + " " + get().number;
		}
		
		@Override
		public byte[] asBytes()
		{
			ByteArrayBuilder builder = new ByteArrayBuilder();
			builder.add(get().string);
			builder.add(get().number);
			return builder.toPrimitiveArray();
		}
		
		@Override
		protected Value<TestValue, TestValue> makeCopy()
		{
			return new CompoundArgumentValueTest(new TestValue(get().string, get().number));
		}
		
		@Override
		protected boolean checkDataEquality(Value<?, ?> value)
		{
			return value instanceof CompoundArgumentValueTest && ((CompoundArgumentValueTest)value).get().equals(get());
		}
		
		@TypeRegistry.ByteConsumer
		public static Consumer<Value<?, TestValue>, Byte> getByteConsumer()
		{
			return (source) ->
			{
				Consumer.ConsumptionResult<Value<?, String>, Byte> stringResult = StringValue.getByteConsumer().consume(source);
				if (!stringResult.success())
					return new Consumer.ConsumptionResult<>(source, RichStringBuilder.start("Can't parse string: ").append(stringResult.description()).build());
				
				Consumer.ConsumptionResult<Value<?, Integer>, Byte> numberResult = IntegerValue.getByteConsumer().consume(source);
				if (!numberResult.success())
					return new Consumer.ConsumptionResult<>(source, RichStringBuilder.start("Can't parse number: ").append(numberResult.description()).build());
				
				return new Consumer.ConsumptionResult<>(new CompoundArgumentValueTest(new TestValue(((StringValue)stringResult.value()).get(), ((IntegerValue)numberResult.value()).get())), source);
			};
		}
		
		@TypeRegistry.ValidationValues
		public static Value<?, TestValue>[] validationValues()
		{
			return new CompoundArgumentValueTest[] {
					new CompoundArgumentValueTest(new TestValue("hey", 1))
			};
		}
		
		@TypeRegistry.ArgumentList
		public static AdaptiveArgumentList argumentList(Object[] initData)
		{
			AdaptiveArgumentList list = new AdaptiveArgumentList(false);
			
			list.addArgument(StringValue.class);
			list.addArgument(IntegerValue.class);
			
			return list;
		}
		
		@TypeRegistry.BuildValue
		public static TestValue valueBuilder(AdaptiveInterpretation result, Executor executor)
		{
			return new TestValue((String)result.value()[0], (Integer)result.value()[1]);
		}
	}
}