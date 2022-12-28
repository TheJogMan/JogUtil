package jogUtil.data.values;

import jogUtil.*;
import jogUtil.commander.*;
import jogUtil.data.*;
import jogUtil.indexable.*;

import java.util.*;

public class UUIDValue extends Value<UUID, UUID>
{
	public UUIDValue()
	{
		super();
	}
	
	public UUIDValue(Object[] initData)
	{
		super(initData);
	}
	
	public UUIDValue(UUID id)
	{
		super(id);
	}
	
	@Override
	public String defaultName()
	{
		return "UUID";
	}
	
	@Override
	public List<String> argumentCompletions(Indexer<Character> source, Executor executor, Object[] data)
	{
		return null;
	}
	
	@Override
	public UUID emptyValue()
	{
		return UUID.randomUUID();
	}
	
	@Override
	public String asString()
	{
		return get().toString();
	}
	
	@Override
	public byte[] asBytes()
	{
		return toByteData(get());
	}
	
	@Override
	protected Value<UUID, UUID> makeCopy()
	{
		return new UUIDValue(get());
	}
	
	@Override
	protected boolean checkDataEquality(Value<?, ?> value)
	{
		return value instanceof UUIDValue && ((UUIDValue)value).get().equals(get());
	}
	
	@Override
	public void initArgument(Object[] args)
	{
	
	}
	
	@TypeRegistry.ByteConsumer
	public static Consumer<Value<?, UUID>, Byte> getByteConsumer()
	{
		return (source) ->
		{
			ArrayList<Byte> bytes = source.allNext(16);
			if (bytes == null)
				return new Consumer.ConsumptionResult<>(source, "Not enough available data.");
			return new Consumer.ConsumptionResult<>(new UUIDValue(fromByteData(bytes)), source);
		};
	}
	
	@TypeRegistry.CharacterConsumer
	public static Consumer<Value<?, UUID>, Character> getCharacterConsumer()
	{
		return (source) ->
		{
			String string = StringValue.consumeString(source, 36, true);
			if (string == null)
				return new Consumer.ConsumptionResult<>(source, "Not enough available data.");
			try
			{
				return new Consumer.ConsumptionResult<>(new UUIDValue(UUID.fromString(string)), source);
			}
			catch (Exception e)
			{
				return new Consumer.ConsumptionResult<>(source, "Invalid UUID format.");
			}
		};
	}
	
	@TypeRegistry.ValidationValues
	public static Value<?, UUID>[] validationValues()
	{
		return new UUIDValue[] {
			new UUIDValue(UUID.fromString("4cd96274-43a8-474e-8658-a1ccd24d67ac")),
			new UUIDValue(UUID.fromString("e7cf7766-1a77-474f-8a99-d0d7d9e9a711")),
			new UUIDValue(UUID.fromString("f8331f26-1fef-40e2-8099-ac2b7fb62505"))
		};
	}
	
	public static byte[] toByteData(UUID id)
	{
		byte[] most = LongValue.toByteData(id.getMostSignificantBits());
		byte[] least = LongValue.toByteData(id.getLeastSignificantBits());
		byte[] data = new byte[16];
		for (int index = 0; index < 8; index++)
		{
			data[index] = most[index];
			data[index + 8] = least[index];
		}
		return data;
	}
	
	public static UUID fromByteData(byte[] byteData)
	{
		byte[] most = new byte[8];
		byte[] least = new byte[8];
		for (int index = 0; index < 8; index++)
		{
			most[index] = byteData[index];
			least[index] = byteData[index + 8];
		}
		return new UUID(LongValue.fromByteData(most), LongValue.fromByteData(least));
	}
	
	public static UUID fromByteData(Byte[] byteData)
	{
		return fromByteData(ByteArrayBuilder.toPrimitive(byteData));
	}
	
	public static UUID fromByteData(Collection<Byte> byteData)
	{
		return fromByteData(ByteArrayBuilder.toPrimitive(byteData));
	}
}