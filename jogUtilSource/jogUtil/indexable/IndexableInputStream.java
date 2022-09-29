package jogUtil.indexable;


import java.io.*;
import java.util.*;

public class IndexableInputStream extends Indexable<Byte>
{
	ArrayList<Byte> data = new ArrayList<>();
	InputStream stream;
	EatingThread thread;
	boolean complete = false;
	
	public IndexableInputStream(InputStream stream)
	{
		this.stream = stream;
		thread = new EatingThread();
		thread.start();
	}
	
	private class EatingThread extends Thread
	{
		boolean stopped = false;
		
		@Override
		public void run()
		{
			try
			{
				while (!stopped)
				{
					byte[] buffer = new byte[stream.available()];
					if (buffer.length == 0)
						buffer = new byte[1];
					int amount = stream.read(buffer);
					if (amount == -1)
						break;
					else
					{
						for (byte value : buffer)
							data.add(value);
						wakeWaiters();
					}
				}
			}
			catch (IOException ignored)
			{
			
			}
			try
			{
				stream.close();
			}
			catch (IOException ignored)
			{
			
			}
			complete = true;
			wakeWaiters();
		}
	}
	
	/**
	 * Causes the reading thread to stop even if it hasn't yet reached the end of the source.
	 * <p>
	 *     Will wait for the eating thread to terminate.
	 * </p>
	 */
	public void stop()
	{
		thread.stopped = true;
		while (thread.isAlive())
		{
			try
			{
				thread.join();
			}
			catch (InterruptedException ignored)
			{
			
			}
		}
	}
	
	@Override
	public Byte get(int index)
	{
		return data.get(index);
	}
	
	@Override
	public void set(int index, Byte value)
	{
	
	}
	
	@Override
	public boolean complete()
	{
		return complete;
	}
	
	@Override
	public int size()
	{
		return data.size();
	}
	
	@Override
	public boolean contains(Object o)
	{
		return data.contains(o);
	}
	
	@Override
	public boolean add(Byte aByte)
	{
		return false;
	}
	
	@Override
	public boolean remove(Object o)
	{
		return false;
	}
	
	@Override
	public void clear()
	{
	
	}
}