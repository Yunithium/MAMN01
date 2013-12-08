import java.util.ArrayList;

public class Test{
	public static void main(String[] args){
		ArrayList<String> src = new ArrayList<String>();
		src.add("Yunus");
		src.add("Emre");
		
		// This doesn't create a shallow copy of the first ArrayList.
		ArrayList<String> dest = new ArrayList<String>(src);
		
		src.set(1, "Taskin");
		
		System.out.println(src.get(1));
		System.out.println(dest.get(1));
	}
}