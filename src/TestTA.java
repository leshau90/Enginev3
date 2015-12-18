import util.*;
public class TestTA {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length<1 || args.length>2){
			System.out.println("incorrect  amount of arguments... ");
			System.out.println("usage: TestTA <Jawaban> <Kunci> ");
			
		}
		else if (args.length==1){
			System.out.println("start test parse...");
			//Analyze.notSemantic(args[0]);
			//Analyze.justOne(args[0]);
		}
		else {
			String a = Analyze.isItTrue(args[0], args[1]);			
			System.out.println(a);
		}
	}
	
	static int selectKth(int a[], int k, int n)
	{	int i, j, mini, tmp;
		for (i = 0; i < k; i++)
		{	mini = i;
			for (j = i+1; j < n; j++)
				if (a[j] < a[mini])
					mini = j;
			tmp = a[i];
			a[i] = a[mini];
			a[mini] = tmp;
		}
		return a[k-1];
	}

}
