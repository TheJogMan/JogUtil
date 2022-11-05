package jogUtil;

import jogUtil.data.*;
import jogUtil.data.values.*;
import jogUtil.indexable.*;

import java.util.*;

public class ByteArrayBuilder
{
	final ArrayList<Byte> data = new ArrayList<>();
	
	public ByteArrayBuilder()
	{
	
	}
	
	public ByteArrayBuilder(Byte[] data)
	{
		add(data);
	}
	
	public ByteArrayBuilder(byte[] data)
	{
		add(data);
	}
	
	public void add(byte value)
	{
		data.add(value);
	}
	
	public void add(Byte value)
	{
		data.add(value);
	}
	
	public void add(byte[] data)
	{
		for (byte datum : data)
			this.data.add(datum);
	}
	
	public void add(Byte[] data)
	{
		this.data.addAll(Arrays.asList(data));
	}
	
	public void add(int value)
	{
		add(IntegerValue.toByteData(value));
	}
	
	public void add(long value)
	{
		add(LongValue.toByteData(value));
	}
	
	public void add(short value)
	{
		add(ShortValue.toByteData(value));
	}
	
	public void add(boolean value)
	{
		add(BooleanValue.toByteData(value));
	}
	
	public void add(float value)
	{
		add(FloatValue.toByteData(value));
	}
	
	public void add(double value)
	{
		add(DoubleValue.toByteData(value));
	}
	
	public void add(UUID id)
	{
		add(UUIDValue.toByteData(id));
	}
	
	public void add(Value<?, ?> value)
	{
		add(value.asBytes());
	}
	
	public void add(String value)
	{
		add(StringValue.toByteData(value));
	}
	
	public void add(char value)
	{
		add(CharacterValue.toByteData(value));
	}
	
	public void add(Data data)
	{
		add(data.toByteData());
	}
	
	public int size()
	{
		return data.size();
	}
	
	public byte get(int index)
	{
		return data.get(index);
	}
	
	public void set(int index, byte byt)
	{
		data.set(index, byt);
	}
	
	public void remove(int index)
	{
		data.remove(index);
	}
	
	public byte[] toPrimitiveArray()
	{
		byte[] data = new byte[this.data.size()];
		for (int index = 0; index < data.length; index++)
			data[index] = this.data.get(index);
		return data;
	}
	
	public Byte[] toObjectArray()
	{
		return data.toArray(new Byte[0]);
	}
	
	public static byte[] toPrimitive(Collection<Byte> bytes)
	{
		byte[] data = new byte[bytes.size()];
		int index = 0;
		for (Byte byt : bytes)
		{
			data[index] = byt != null ? byt : 0;
			index++;
		}
		return data;
	}
	
	public static byte[] toPrimitive(Byte[] bytes)
	{
		byte[] data = new byte[bytes.length];
		for (int index = 0; index < data.length; index++)
			data[index] = bytes[index];
		return data;
	}
	
	public static Byte[] toObject(byte[] bytes)
	{
		Byte[] data = new Byte[bytes.length];
		for (int index = 0; index < data.length; index++)
			data[index] = bytes[index];
		return data;
	}
	
	public static Indexer<Byte> indexer(Byte[] data)
	{
		return (new IndexableArray<>(data)).iterator();
	}
	
	public static Indexer<Byte> indexer(byte[] data)
	{
		return (new IndexableArray<>(toObject(data))).iterator();
	}
}