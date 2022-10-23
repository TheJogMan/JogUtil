package jogUtil;

import jogUtil.data.*;

public class Main
{
	public static void main(String[] args)
	{
		Result[] results = TypeRegistry.defaultValueStatus();
		for (Result result : results)
			System.out.println(result.description());
	}
}