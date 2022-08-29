package lrec.extraction;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class Sentence
{
	public Integer h;
	public String[][] tab;
	public final Integer w=6;
	final Logger logger = LoggerFactory.getLogger(Sentence.class);
	public Sentence(InputStreamReader is)
	{
		try 
		{
			this.readSentence(is);
		} 
		catch (IOException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	/*à vérifier le fonctionnement, pour un segment de texte annoté identifié comme phrase et mis dans un inputstream */
	public void readSentence(InputStreamReader is) throws IOException
	{
	
		BufferedReader br=new BufferedReader(is);
		String line="";
		String[] str;
		
		
		Queue<String[]> queue=new LinkedList<String[]>();
		
		while((line=br.readLine())!=null)
		{
//			this.logger.info("processing line {}",line);
			str=line.split("\t");
//			this.logger.info("line to tab {}",Arrays.asList(str));
			if(!line.isEmpty())
			{
				queue.add(str);
			}
			
		}
		this.h=queue.size();
		
		this.tab=new String[h][this.w];
		for(int i=0;i<h;i++) //h-1
		{
			String []temp=queue.poll();
//			this.logger.info("queue poll {}",Arrays.asList(temp));
			
			for(int j=0;j<w; j++)
			{
				tab[i][j]=temp[j];
			}
		}
		
//		logger.info("current tab {}",Arrays.toString(tab));
	}
	
	public String getValue(int a,int b)
	{
		return this.tab[a][b];
	}
	
	public Map<String,String>nodes=new HashMap<String,String>();
	
	public void extract()
	{
		
	}
	
	
}
