/**
 * Created for coe 1501 Project 3
 * I thought I was going to be clever and make a generic MultiHeap Structure that could mimic database functionality for
 * Java object. It ended up being more pain than it was worth, and is all around a bad design.
 *
 * I'm leaving out javadoc on purpose to encourage others to come up with better designs.
 * No one should use this.
 *
 * @author Tyler Mohnke
 */

import java.util.HashMap;
import java.util.Comparator;
import java.util.ArrayList;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Scanner;

public class CarTracker
{
    private static HashMap<String, Car> vinMap = new HashMap<String, Car>();
    
    public static void main(String... args) throws Exception
    {
        BufferedReader input = new BufferedReader(new FileReader(new File("cars.txt")));
        input.readLine();
        
        MultiHeap<Car> multiHeap = new MultiHeap<Car>();
        PriceComparator defaultPriceComparator = new PriceComparator(null);
        MileageComparator defaultMileageComparator = new MileageComparator(null);
        
        while(input.ready())
        {
            String line = input.readLine();
            String[] carFields = line.split(":");
            
            String makeModel = carFields[1] + carFields[2];
            Car car = new Car(carFields[0], carFields[1], carFields[2], Integer.decode(carFields[3]), Integer.decode(carFields[4]), carFields[5]);
            
            vinMap.put(carFields[0], car);
            
            multiHeap.add(car, defaultPriceComparator);
            multiHeap.add(car, defaultMileageComparator);
            multiHeap.add(car, new PriceComparator(makeModel));
            multiHeap.add(car, new MileageComparator(makeModel));
        }
        input.close();
        
        StringBuilder usage = new StringBuilder();
        usage.append("Usage:\n");
        usage.append("    add <vin> <make> <model> <price> <mileage> <color>\n");
        usage.append("    update <vin> [price | mileage | color] <new_value>\n");
        usage.append("    remove <vin>\n");
        usage.append("    lowest_price [<make> <model> | none]\n");
        usage.append("    lowest_mileage [<make> <model> | none]\n");
        usage.append("    exit\n");
        System.out.println(usage.toString());
        usage = null;
        
        Scanner userInputer = new Scanner(System.in);
        boolean running = true;
        while(running)
        {
            String userInput = userInputer.next();
            
            switch(userInput)
            {
                case "add":
                {
                    Car car = new Car(userInputer.next(), userInputer.next(), userInputer.next(), 
                                      userInputer.nextInt(), userInputer.nextInt(), userInputer.next());
                    
                    vinMap.put(car.m_Vin, car);
                    multiHeap.add(car, defaultPriceComparator);
                    multiHeap.add(car, defaultMileageComparator);
                    multiHeap.add(car, new PriceComparator(car.m_MakeAndModel));
                    multiHeap.add(car, new MileageComparator(car.m_MakeAndModel));
                }
                break;
                
                case "update":
                {
                    String vin = userInputer.next();
                    Car car = vinMap.get(vin);
                    
                    if(car == null)
                    {
                        System.out.println("Car not found");
                        break;
                    }
                    
                    switch(userInputer.next())
                    {
                        case "price":
                            car.m_Price = userInputer.nextInt();
                        break;
                        
                        case "mileage":
                            car.m_Mileage = userInputer.nextInt();
                        break;
                        
                        case "color":
                            car.m_Color = userInputer.next();
                        break;
                    }
                    
                    multiHeap.update(car);
                }
                break;
                
                case "remove":
                {
                    String vin = userInputer.next();
                    Car car = vinMap.get(vin);
                    
                    if(car == null)
                    {
                        System.out.println("Car not found");
                    }
                    
                    multiHeap.remove(car);
                    System.out.println(car);
                }
                break;
                
                case "lowest_price":
                {
                    String makeModel = userInputer.next();
                    if(makeModel.equals("none"))
                    {
                        makeModel = null;
                    }
                    else
                    {
                        makeModel += userInputer.next();
                    }
                    
                    PriceComparator comparator = new PriceComparator(makeModel);
                    System.out.println(multiHeap.peek(comparator));
                }
                break;
                
                case "lowest_mileage":
                {
                    String makeModel = userInputer.next();
                    if(makeModel.equals("none"))
                    {
                        makeModel = null;
                    }
                    else
                    {
                        makeModel += userInputer.next();
                    }
                    
                    MileageComparator comparator = new MileageComparator(makeModel);
                    System.out.println(multiHeap.peek(comparator));
                }
                break;
                
                case "exit":
                {
                    running = false;
                }
                break;
                
                default:
                {
                    System.out.println("Unsupported operation");
                }
            }
            
            System.out.println();
        }
    }
}

class MileageComparator implements Comparator<Car>
{
    String m_MakeModel;
    
    public MileageComparator(String makeModel)
    {
        m_MakeModel = makeModel;
    }
    
    // Cars of different make are "larger" than cars of this make
    public int compare(Car o1, Car o2)
    {
        if(o1.m_MakeAndModel.equals(m_MakeModel))
        {
            if(o2.m_MakeAndModel.equals(m_MakeModel))
            {
                return o1.m_Mileage - o2.m_Mileage;
            }
            else
            {
                return -1;
            }
        }
        else
        {
            if(o2.m_MakeAndModel.equals(m_MakeModel))
            {
                return 1;
            }
            else
            {
                return o1.m_Mileage - o2.m_Mileage;
            }
        }
    }
    
    @Override
    public boolean equals(Object obj)
    {
        if(obj == this)
        {
            return true;
        }
        
        if(obj instanceof MileageComparator)
        {
            MileageComparator other = (MileageComparator)obj;
            if(m_MakeModel == null)
            {
                return other.m_MakeModel == null;
            }
            return this.m_MakeModel.equals(other.m_MakeModel);
        }
        
        return false;
    }
    
    @Override
    public int hashCode()
    {
        if(m_MakeModel == null)
        {
            return 0;
        }
        else
        {
            return m_MakeModel.hashCode();
        }
    }
}

class PriceComparator implements Comparator<Car>
{
    String m_MakeModel;
    
    public PriceComparator(String makeModel)
    {
        m_MakeModel = makeModel;
    }
    
    // Cars of different make are "larger" than cars of this make
    public int compare(Car o1, Car o2)
    {
        if(o1.m_MakeAndModel.equals(m_MakeModel))
        {
            if(o2.m_MakeAndModel.equals(m_MakeModel))
            {
                return o1.m_Price - o2.m_Price;
            }
            else
            {
                return -1;
            }
        }
        else
        {
            if(o2.m_MakeAndModel.equals(m_MakeModel))
            {
                return 1;
            }
            else
            {
                return o1.m_Price - o2.m_Price;
            }
        }
    }
    
    @Override
    public boolean equals(Object obj)
    {
        if(obj == this)
        {
            return true;
        }
        
        if(obj instanceof PriceComparator)
        {
            PriceComparator other = (PriceComparator)obj;
            if(m_MakeModel == null)
            {
                return other.m_MakeModel == null;
            }
            return this.m_MakeModel.equals(other.m_MakeModel);
        }
        
        return false;
    }
    
    @Override
    public int hashCode()
    {
        if(m_MakeModel == null)
        {
            return 0;
        }
        else
        {
            return m_MakeModel.hashCode();
        }
    }
}

// Using generics was wasted engineering effort
// The entire class is a remnant from back when every heap would hold every item, even if it wasn't sorting thata item
// Not enough time to refactor :/
class MultiHeap<T>
{
    private HashMap<Integer, T> m_Items;                // Map Ids to Ts
    private HashMap<Comparator<T>, MinHeap> m_Heaps;    // Map Comparators to their heaps

    private static int idGenerator = 1;
    
    MultiHeap()
    {
        m_Items = new HashMap<Integer, T>();
        m_Heaps = new HashMap<Comparator<T>, MinHeap>();
    }
    
    public void add(T item, Comparator<T> comparator)
    {
        int id = idGenerator++;
        m_Items.put(id, item);
        
        MinHeap heap = m_Heaps.get(comparator);
        if(heap == null)
        {
            heap = new MinHeap(new ComparatorWrapper(comparator));
            m_Heaps.put(comparator, heap);
        }
        
        heap.add(id, item);
    }
    
    public void update(T item)
    {
        for(MinHeap heap : m_Heaps.values())
        {
            heap.update(item);
        }
    }
    
    public void remove(T item)
    {
        for(MinHeap heap : m_Heaps.values())
        {
            int id = heap.remove(item);
            if(id != 0)
            {
                m_Items.remove(id);
            }
        }
    }
    
    public T peek(Comparator<T> comparator)
    {
        T retval = null;
        
        MinHeap heap = m_Heaps.get(comparator);
        if(heap != null)
        {
            retval = heap.peek();
        }
        
        return retval;
    }
    
    public void print()
    {
        for(MinHeap heap : m_Heaps.values())
        {
            heap.print();
            System.out.println();
        }
    }
    
    // The indiretion map maps an T to an index in the heap. The heap stores ids to the Ts in the parent class.
    // Implementation borrowed with added in indirection from an assignment in data structures
    class MinHeap
    {
        private Comparator<Integer> m_Comparator;
        private HashMap<T, Integer> m_IndirectionMap;   // Maps T to an index into the heap
        private ArrayList<Integer> m_Heap;              // Holds ids generated by MultiHeap
        
        public MinHeap(Comparator<Integer> comparator)
        {
            m_Comparator = comparator;
            m_IndirectionMap = new HashMap<T, Integer>();
            m_Heap = new ArrayList<Integer>();
        }
        
        public void add(Integer id, T item)
        {
            m_IndirectionMap.put(item, m_Heap.size());
            m_Heap.add(id);
            reheapUp(m_Heap.size() - 1);
        }
        
        public void update(T item)
        {
            Integer index = m_IndirectionMap.get(item);
            if(index == null)
            {
                return;
            }
            reheapDown(index);
            reheapUp(index);
        }
        
        public int remove(T item)
        {
            Integer index = m_IndirectionMap.get(item);
            if(index == null)
            {
                return 0;
            }
            
            swap(index, m_Heap.size() - 1);
            int id = m_Heap.remove(m_Heap.size() - 1);
            reheapDown(index);
            
            m_IndirectionMap.remove(item);
            
            return id;
        }
        
        public T peek()
        {
            return m_Items.get(m_Heap.get(0));
        }
        
        private void reheapUp(int index)
        {
            int parent = (index - 1) / 2;
            if(parent >= 0)
            {
                if(m_Comparator.compare(m_Heap.get(index), m_Heap.get(parent)) < 0)
                {
                    swap(index, parent);
                    reheapUp(parent);
                }
            }
        }
        
        private void reheapDown(int index)
        {
            int minChild = getMinChildIndex(index);
            if(minChild > -1)
            {
                if(m_Comparator.compare(m_Heap.get(minChild), m_Heap.get(index)) < 0)
                {
                    swap(index, minChild);
                    reheapDown(minChild);
                }
            }
        }
        
        private void swap(int index1, int index2)
        {
            if(index1 == index2)
            {
                return;
            }
            
            T t1 = m_Items.get(m_Heap.get(index1));
            T t2 = m_Items.get(m_Heap.get(index2));
            m_IndirectionMap.put(t1, index2);
            m_IndirectionMap.put(t2, index1);
            
            Integer temp = m_Heap.get(index2);
            m_Heap.set(index2, m_Heap.get(index1));
            m_Heap.set(index1, temp);
        }
        
        private int getMinChildIndex(int index)
        {
            int left = 2 * index + 1;
            if(left >= m_Heap.size())
            {
                return -1;
            }
            else
            {
                int right = 2 * index + 2;
                if((right >= m_Heap.size()) || (m_Comparator.compare(m_Heap.get(left), m_Heap.get(right)) < 0))
                {
                    return left;
                }
                else
                {
                    return right;
                }
            }
        }
        
        public void print()
        {
            for(Integer id : m_Heap)
            {
                System.out.println(m_Items.get(id).toString() + " ");
            }
            System.out.println();
        }
    }
    
    // Used to dereference the ids held in MinHeap to compare the Cars they represent
    class ComparatorWrapper implements Comparator<Integer>
    {
        private Comparator<T> m_UserComparator;
        
        ComparatorWrapper(Comparator<T> userComparator)
        {
            m_UserComparator = userComparator;
        }
        
        public int compare(Integer o1, Integer o2)
        {
            return m_UserComparator.compare(m_Items.get(o1), m_Items.get(o2));
        }
        
        public boolean equals(Object obj)
        {
            return m_UserComparator.equals(obj);
        }
    }
}

class Car
{
    public String m_Vin;
    public String m_MakeAndModel;
    public String m_Make;
    public String m_Model;
    public int m_Price;
    public int m_Mileage;
    public String m_Color;
    
    public Car(String vin, String make, String model, int price, int mileage, String color)
    {
        m_Vin = vin;
        m_Make = make;
        m_Model = model;
        m_MakeAndModel = make + model;
        m_Price = price;
        m_Mileage = mileage;
        m_Color = color;
    }
    
    @Override
    public int hashCode()
    {
        return m_Vin.hashCode();
    }
    
    @Override
    public boolean equals(Object obj)
    {
        if(obj == this)
        {
            return true;
        }
        
        if(obj instanceof Car)
        {
            Car other = (Car)obj;
            return this.m_Vin.equals(other.m_Vin);
        }
        
        return false;
    }
    
    public String toString()
    {
        return m_Vin + " " + m_Make + " " + m_Model + " " + m_Price + " " + m_Mileage + " " + m_Color;
    }
}