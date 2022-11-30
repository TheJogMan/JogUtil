package jogUtil.indexable;

import java.io.*;
import java.util.*;

public class IndexableReader extends Indexable<Character>
{
	final ArrayList<Character> characters = new ArrayList<>();
	final Reader reader;
	final EatingThread thread;
	boolean complete = false;
	
	public IndexableReader(Reader reader)
	{
		this.reader = reader;
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
					char[] buffer = new char[1];
					int amount = reader.read(buffer);
					if (amount == -1)
						break;
					else
					{
						characters.add(buffer[0]);
						wakeWaiters();
					}
				}
			}
			catch (IOException ignored)
			{
			
			}
			try
			{
				reader.close();
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
	public Character get(int index)
	{
		if (index >= 0 && index < characters.size())
			return characters.get(index);
		else
			return null;
	}
	
	@Override
	public void set(int index, Character value)
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
		return characters.size();
	}
	
	@Override
	public boolean contains(Object o)
	{
		return characters.contains(o);
	}
	
	@Override
	public boolean add(Character character)
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