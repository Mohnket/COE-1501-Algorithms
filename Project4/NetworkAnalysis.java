/**
 * Implementation of various graphing algorithms
 * Created for coe 1501 Project 4
 *
 * @author Tyler Mohnke
 */
 
import java.util.ArrayList;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Scanner;
import java.util.PriorityQueue;
import java.util.ArrayDeque;
import java.util.function.BiPredicate;
import java.util.Arrays;
import java.util.function.Function;

public class NetworkAnalysis
{
    private ArrayList<ArrayList<NetworkEdge>> m_AdjacencyGraph;
    
    /**
     * Constructor
     * @param veritces  The number of vertices in the graph
     */
    private NetworkAnalysis(int vertices)
    {
        m_AdjacencyGraph = new ArrayList<ArrayList<NetworkEdge>>(vertices);
        for(int index = 0; index < vertices; ++index)
        {
            m_AdjacencyGraph.add(new ArrayList<NetworkEdge>(vertices));
        }
    }
    
    /**
     * An adjusted version of dijksta's that uses a regular PriorityQueue instead of an indexable one
     * @param start The starting vertex
     * @param end   The vertex being searched for
     * @return Iterable<NetworkEdge>    A 'list' of edges that contain the shortest path from start to end
     */
    private Iterable<NetworkEdge> lowestLatencyPath(int start, int end)
    {
        // Data structures
        double[] distancesTo = new double[m_AdjacencyGraph.size()];
        int[] prevVertex = new int[m_AdjacencyGraph.size()];
        PriorityQueue<Integer> queue = new PriorityQueue<Integer>((Integer v1, Integer v2) -> 
        {
            return Double.compare(distancesTo[v1], distancesTo[v2]);
        });
        boolean[] visited = new boolean[m_AdjacencyGraph.size()];
        
        // Initialize the arrays
        for(int index = 0; index < distancesTo.length; ++index)
        {
            distancesTo[index] = Double.MAX_VALUE;
            prevVertex[index] = -1;
        }
        
        // visit the starting vertex
        distancesTo[start] = 0.0;
        prevVertex[start] = start;
        queue.add(start);
        visited[start] = true;
        
        // for each vertex, check to see if it is closer than we currently think it is, and if so remember the path
        // taken to get there
        while(queue.isEmpty() == false)
        {
            int vertex = queue.poll();
            for(NetworkEdge edge : m_AdjacencyGraph.get(vertex))
            {
                double alternative = distancesTo[edge.m_Start] + edge.m_Latency;
                if(alternative < distancesTo[edge.m_End])
                {
                    distancesTo[edge.m_End] = alternative;
                    prevVertex[edge.m_End] = vertex;
                    
                    if(visited[edge.m_End] == false)
                    {
                        queue.add(edge.m_End);
                        visited[edge.m_End] = true;
                    }
                }
            }
        }
        
        // constuct the path
        ArrayDeque<NetworkEdge> path = new ArrayDeque<NetworkEdge>();
        for(int vertex = end; vertex != start; vertex = prevVertex[vertex])
        {
            if(vertex == -1)
            {
                return null;
            }
            
            for(NetworkEdge edge : m_AdjacencyGraph.get(prevVertex[vertex]))
            {
                if(edge.m_End == vertex)
                {
                    path.push(edge);
                    break;
                }
            }
        }
        
        return path;
    }
    
    /**
     * For edge in the graph, union together its start and end veritces if that edge is copper.
     * @return boolean  true if the graph is connected with only copper edges
     */
    private boolean isCopperConnected()
    {
        QuickUnionUF union = new QuickUnionUF(m_AdjacencyGraph.size());
        
        // for each edge if it is copper add the vertices to the union. If the union is one blob, the graph is copper
        for(ArrayList<NetworkEdge> list : m_AdjacencyGraph)
        {
            if(union.count() == 1)
            {
                break;
            }
            
            for(NetworkEdge edge : list)
            {
                if(edge.m_CableType == Cable.copper)
                {
                    union.union(edge.m_Start, edge.m_End);
                }
                
                if(union.count() == 1)
                {
                    break;
                }
            }
        }
        
        return union.count() == 1;
    }
    
    /**
     * An adjusted Ford Fulkerson
     * I dont know for certain, but I think because data can flow either way through an edge I don't need to think about
     * subtracting flow going through an edge, instead I can just pump it through the other direction.
     * @return int  The max bandwidth between 2 points
     */
    private int findBandwidth(int source, int sink)
    {
        for(ArrayList<NetworkEdge> list : m_AdjacencyGraph)
        {
            for(NetworkEdge edge : list)
            {
                edge.m_Flow = 0;
            }
        }
        
        // A local function that returns true if there exists a path from start to end, and fills the path array with
        // the path it found.
        // There are better ways to do this. This could've been its own function that returned null if a path wasnt
        // found
        NetworkEdge[] path = new NetworkEdge[m_AdjacencyGraph.size()];
        BiPredicate<Integer, Integer> augmentedBreadthFirstSearch = (Integer start, Integer end) ->
        {
            boolean[] visited = new boolean[m_AdjacencyGraph.size()];
            
            ArrayDeque<Integer> queue = new ArrayDeque<Integer>();
            queue.add(start);
            visited[start] = true;
            while((queue.isEmpty() == false) && (visited[end] == false))
            {
                int vertex = queue.poll();
                for(NetworkEdge edge : m_AdjacencyGraph.get(vertex))
                {
                    if((edge.m_Bandwtih - edge.m_Flow > 0) && (visited[edge.m_End] == false))
                    {
                        path[edge.m_End] = edge;
                        visited[edge.m_End] = true;
                        queue.add(edge.m_End);
                    }
                }
            }
            
            return visited[end];
        };
        
        // while there are paths, add the maximum flow along that path
        int value = 0;
        while(augmentedBreadthFirstSearch.test(source, sink) == true)
        {
            int maxBandwidth = Integer.MAX_VALUE;
            for(int vertex = sink; vertex != source; vertex = path[vertex].m_Start)
            {
                int residual = path[vertex].m_Bandwtih - path[vertex].m_Flow;
                maxBandwidth = (maxBandwidth < residual) ? maxBandwidth : residual;
            }
            for(int vertex = sink; vertex != source; vertex = path[vertex].m_Start)
            {
                path[vertex].m_Flow += maxBandwidth;
            }
            
            value += maxBandwidth;
        }
        
        return value;
    }
    
    /**
     * Kruskal. Implementation of https://en.wikipedia.org/wiki/Kruskal%27s_algorithm#Pseudocode
     * @return ArrayList<NetworkEdge> All the edges in the minimum spanning tree.
     */
    private ArrayList<NetworkEdge> findMinimumLatencySpanningTree()
    {
        // data structures
        ArrayList<NetworkEdge> retval = new ArrayList<NetworkEdge>();
        QuickFindUF union = new QuickFindUF(m_AdjacencyGraph.size());
        PriorityQueue<NetworkEdge> queue = new PriorityQueue<NetworkEdge>((NetworkEdge e1, NetworkEdge e2) ->
        {
            return Double.compare(e1.m_Latency, e2.m_Latency);
        });
        
        // Add all edges to the queue
        for(ArrayList<NetworkEdge> list : m_AdjacencyGraph)
        {
            for(NetworkEdge edge : list)
            {
                queue.add(edge);
            }
        }
        
        // pop the shortest edge out of the queue, join the vertices if they are not already joined
        while((union.count() > 1) && (queue.isEmpty() == false))
        {
            NetworkEdge edge = queue.poll();
            if(union.connected(edge.m_Start, edge.m_End) == false)
            {
                retval.add(edge);
                union.union(edge.m_Start, edge.m_End);
            }
        }
        
        if(union.count() > 1)
        {
            return null;
        }
        else
        {
            return retval;
        }
    }
    
    /**
     * This would be easier to read with function pointers and reference types and namespaces or if I was good at coding
     *
     * 'removes' a vertex from the graph and then checks if there is an articualtion point.
     * If there is an articulation, then there exists two vertices that if removed disconnect the graph
     *
     * @return boolean  true if redundant
     */
    private boolean isRedundant()
    {
        // datastuctures for hasArticulationPoint
        int[] parents = new int[m_AdjacencyGraph.size()];
        boolean[] visited = new boolean[m_AdjacencyGraph.size()];
        int[] depths = new int[m_AdjacencyGraph.size()];
        int[] low = new int[m_AdjacencyGraph.size()];
        
        // Helper class becuase recursive lambdas aren't explicitly allowed by java, apparently
        class Recursive<T>
        {
            public T func;
        }
        Recursive<TriFunction<Integer, Integer, Integer, Boolean>> hasArticulationPoint = 
                                                       new Recursive<TriFunction<Integer, Integer, Integer, Boolean>>();
        // hasArticulationPoint is a local function because I don't want to pollute the class scope with the
        // datastuctures needed for it. Though if I did, it would look prettier.
        hasArticulationPoint.func = (Integer vertex, Integer depth, Integer ignored) ->
        {
            // implementation of https://en.wikipedia.org/wiki/Biconnected_component#Pseudocode
            // I'm fairly certain it does the single depth first search that we covered in class
            visited[vertex] = true;
            depths[vertex] = depth;
            low[vertex] = depth;
            int childCount = 0;
            boolean isArticulation = false;
            for(NetworkEdge edge : m_AdjacencyGraph.get(vertex))
            {
                int child = edge.m_End;
                if(child == ignored)
                {
                    continue;
                }
                
                if(visited[child] == false)
                {
                    parents[child] = vertex;
                    isArticulation = hasArticulationPoint.func.apply(child, depth + 1, ignored);
                    childCount++;
                    if((parents[vertex] != vertex) && (low[child] >= depths[vertex]))
                    {
                        isArticulation = true;
                    }
                    low[vertex] = (low[vertex] < low[child]) ? low[vertex] : low[child];
                }
                else if(child != parents[vertex])
                {
                    low[vertex] = (low[vertex] < depths[child]) ? low[vertex] : depths[child];
                }
                else
                {
                    
                }
            }
            
            if(parents[vertex] == vertex)
            {
                return isArticulation || childCount > 1;
            }
            else
            {
                return isArticulation;
            }
        };        
        
        // first check roots 1 and ignores 0, all other checks root 0 and ignore 1..size
        for(int index = 0; index < parents.length; ++index)
        {
            parents[index] = 1;
        }
        boolean retval = hasArticulationPoint.func.apply(1, 0, 0);
        for(int ignore = 1;(ignore < m_AdjacencyGraph.size()) && (retval == false); ++ignore)
        {
            // re init the datastructures
            for(int index = 0; index < m_AdjacencyGraph.size(); ++ index)
            {
                parents[index] = 0;
                visited[index] = false;
                depths[index] = 0;
                low[index] = 0;
            }
            
            retval = hasArticulationPoint.func.apply(0, 0, ignore);
        }
        
        return retval == false;
    }
    
    /**
     * Entry point of the program
     * @param args  A path to an input file
     */
    public static void main(String... args) throws Exception
    {
        BufferedReader input = new BufferedReader(new FileReader(args[0]));
        NetworkAnalysis analysis = new NetworkAnalysis(Integer.decode(input.readLine()));
        
        while(input.ready())
        {
            String[] tokens = input.readLine().split(" ");
            int start = Integer.decode(tokens[0]);
            int end = Integer.decode(tokens[1]);
            String cable = tokens[2];
            int bandwidth = Integer.decode(tokens[3]);
            int length = Integer.decode(tokens[4]);
            
            analysis.m_AdjacencyGraph.get(start).add(new NetworkEdge(start, end, cable, bandwidth, length));
            analysis.m_AdjacencyGraph.get(end).add(new NetworkEdge(end, start, cable, bandwidth, length));
        }
        
        input.close();
        analysis.userInterface();
    }
    
    /**
     * Engine of the user interaction with the graph
     */
    private void userInterface()
    {
        Scanner input = new Scanner(System.in);
        StringBuilder usage = new StringBuilder();
        usage.append("usage:\n\n");
        usage.append("latency [start] [end]\n");
        usage.append("    finds the lowest latency path between two vertices\n");
        usage.append("copper\n");
        usage.append("    checks to see if the graph remains cinnected when only considering copper links\n");
        usage.append("bandwidth [start] [end]\n");
        usage.append("    finds the maximum bandwidth between two points\n");
        usage.append("spanning\n");
        usage.append("    finds the minimum spanning tree of the graph\n");
        usage.append("redundant\n");
        usage.append("    checks if the graph remains connected if any two vertices are removed\n");
        usage.append("exit\n");
        usage.append("    exits\n");
        System.out.println(usage.toString());
        
        while(true)
        {
            String command = input.next();
            switch(command)
            {
                case "latency":
                {
                    int start = input.nextInt();
                    int end = input.nextInt();
                    Iterable<NetworkEdge> path = lowestLatencyPath(start, end);
                    if(path == null)
                    {
                        System.out.println("No path found");
                    }
                    else
                    {
                        int bandwidth = Integer.MAX_VALUE;
                        for(NetworkEdge edge : path)
                        {
                            bandwidth = (bandwidth < edge.m_Bandwtih) ? bandwidth : edge.m_Bandwtih;
                            System.out.print("" + edge.m_Start + " ");
                        }
                        System.out.println(end);
                        System.out.println("Bandwidth: " + bandwidth);
                    }
                }
                break;
                
                case "copper":
                {
                    System.out.println(isCopperConnected());
                }
                break;
                
                case "bandwidth":
                {
                    System.out.println(findBandwidth(input.nextInt(), input.nextInt()));
                }
                break;
                
                case "spanning":
                {
                    ArrayList<NetworkEdge> tree = findMinimumLatencySpanningTree();
                    if(tree == null)
                    {
                        System.out.println("Disconnected Graph; no tree found");
                    }
                    else
                    {
                        for(NetworkEdge edge : tree)
                        {
                            System.out.println(edge);
                        }
                    }
                }
                break;
                
                case "redundant":
                {
                    System.out.println(isRedundant());
                }
                break;
                
                case "exit":
                {
                    System.exit(0);
                }
                break;
                
                default:
                {
                    System.out.println("Unknown command: " + command);
                    System.exit(1);
                }
                break;
            }
            System.out.println();
        }
    }
}

// Misc classes that I don't want to give their own file

class NetworkEdge
{
    static final double OPTICAL_SPEED = 200000000;
    static final double COPPER_SPEED = 230000000;
    
    public int m_Start;
    public int m_End;
    public Cable m_CableType;
    public int m_Bandwtih;
    public int m_Length;
    public double m_Latency;
    
    public int m_Flow;
    
    public NetworkEdge(int start, int end, String cable, int bandwidth, int length)
    {
        m_Start = start;
        m_End = end;
        m_CableType = Cable.valueOf(cable);
        m_Bandwtih = bandwidth;
        m_Length = length;
        switch(m_CableType)
        {
            case optical:
                m_Latency = (double)length/OPTICAL_SPEED;
                break;
            case copper:
                m_Latency = (double)length/COPPER_SPEED;
                break;
        }
    }
    
    public String toString()
    {
        return "" + m_Start + " <-> " + m_End;
    }
}

enum Cable
{
    optical, copper;
}

@FunctionalInterface
interface TriFunction<A,B,C,R>
{
    R apply(A a, B b, C c);

    default <V> TriFunction<A, B, C, V> andThen(Function<? super R, ? extends V> after)
    {
        if(after != null)
        {
            return (A a, B b, C c) -> after.apply(apply(a, b, c));
        }
        else
        {
            return null;
        }
    }
}