package jogUtil.richText;

public class RichColor
{
	public static final RichColor CLEAR = new RichColor(0, 0, 0, 0);
	public static final RichColor WHITE = new RichColor(255, 255, 255, 255);
	
	int color;
	
	public RichColor(int r, int g, int b, int a)
	{
		color = a << 24 | r << 16 | g << 8 | b;
	}
}