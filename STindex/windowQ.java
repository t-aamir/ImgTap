package spatialindex.rtree;
import java.io.*;
import java.util.*;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Vector;

import spatialindex.rtree.RTree;
import spatialindex.spatialindex.Point;
import spatialindex.storagemanager.DiskStorageManager;
import spatialindex.storagemanager.IBuffer;
import spatialindex.storagemanager.IStorageManager;
import spatialindex.storagemanager.PropertySet;
import documentindex.InvertedFile;
import spatialindex.spatialindex.RtreeEntry;
import spatialindex.rtree.Node;
import spatialindex.spatialindex.NNEntry;
//import spatialindex.spatialindex.NNentry_dist;
import spatialindex.spatialindex.Region;
//import storage.WeightEntry;
import storage.DocumentStore;
//import spatialindex.rtree.obj_node;
import spatialindex.spatialindex.IShape;
//import storage.WeightEntryComparator;
import java.util.Enumeration;
import spatialindex.spatialindex.*;
public class windowQ{
    
    public static void main(String[] args)throws Exception
    {
        if(args.length != 2){
            System.out.println("Usage: LKT index_file query_file ");
            System.exit(-1);
        }
        String index_file = args[0];
        String query_file = args[1];
        
        PropertySet ps = new PropertySet();
        ps.setProperty("FileName", index_file + ".rtree");
        IStorageManager diskfile = new DiskStorageManager(ps);
        PropertySet ps2 = new PropertySet();
        Integer i = new Integer(1); // INDEX_IDENTIFIER_GOES_HERE (suppose I know that in this case it is equal to 1);
        ps2.setProperty("IndexIdentifier", i);
        
        RTree tree = new RTree(ps2, diskfile);
        
        
        FileInputStream fis = new FileInputStream(query_file);
        BufferedReader br = new BufferedReader(new InputStreamReader(fis));
        int count = 0;
        String line;
        String[] temp;
        int id = -1;
        double x, y,x2,y2;
        double[] f = new double[2];
        double[] f2 = new double[2];
        
        windowQ w = new windowQ();
        
        while((line = br.readLine()) != null){
            temp = line.split(",");
            
            id = Integer.parseInt(temp[0]);
            x = Double.parseDouble(temp[1]);
            y = Double.parseDouble(temp[2]);
            x2 = Double.parseDouble(temp[3]);
            y2 = Double.parseDouble(temp[4]);
            f[0] = x; f[1] = y;
            f2[0] = x2; f2[1] = y2;
            
            Region q = new Region(new Point(f),new Point(f2));
            System.out.println("query " + count);
            count++;
            
            w.windowQuery(q,tree);
        }
    }
    
    public void windowQuery(Region q, RTree tree)
    {
        PriorityQueue queue = new PriorityQueue(100, new NNEntryComparator());
        
        //start from root node
        RtreeEntry e = new RtreeEntry(tree.m_rootID, false);
        queue.add(new NNEntry(e,  0.0));
        int count = 0;
        while (queue.size() != 0){
            NNEntry first = (NNEntry) queue.poll();
            e = (RtreeEntry)first.m_pEntry;
            
            //if the node is a data object, check if it's inside the query region, if yes --> result
            if(e.isLeafEntry){
                Region obj = (Region)e.getShape();
                if(q.contains(obj))
                {
                    System.out.println(e.getIdentifier() + " " + obj.m_pLow[0]+ " " +obj.m_pLow[1]);
                    count++;
                }
                
            }
            else{
                Node n = tree.readNode(e.getIdentifier());
                for (int cChild = 0; cChild < n.m_children; cChild++)
                {
                    //check for each children if that intersects/contains in the query region. if yes --> add to the queue.
                    if(q.contains(n.m_pMBR[cChild]) || q.intersects(n.m_pMBR[cChild]))
                    {
                        if (n.m_level == 0)
                            e = new RtreeEntry(n.m_pIdentifier[cChild],n.m_pMBR[cChild], true);
                        
                        else
                            e = new RtreeEntry(n.m_pIdentifier[cChild],n.m_pMBR[cChild],false);
                        
                        queue.add(new NNEntry(e, 0));
                    }
                }
            }
        }
    }
}




