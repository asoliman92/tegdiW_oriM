# Getting Started
This is an implementation for widget service handling concurrent reads/writes.
I'm going to explain design decisions here and share references used.


### Before starting
There are some design decisions I went with which we can argue about whether they're the best or not, and 
of course it depends heavily on what we agree on as a team and what to sacrifice and what not.
For this task I focused mainly on the performance trying not to waste resources as I imagine it will be 
real time and performance is of an essence.

- System is designed for heavy read load, and I was favoring performance for reading to writing.
- I'm sacrificing Immutability for performance to update it in place and to be able to have multiple references for same object.
- Maintaining multiple references for the object to get it quicker.

### Overview

- Main class here is `WidgetCustomRepo` as it contains all widgets in this plane.
- `WidgetCustomRepo` has 3 main components: (Going to discuss in details why chose every thing)
    - An ArrayList for keeping the widgets, it will always be sorted.
    - HashMap for references in the ArrayList with widget's id as key.
    - HashMap for references in the ArrayList with widget's zIndex as key.
    
- For locking mechanism used StampedLock with optimistic reads for parts and normal locking reads for others.


### Deep dive
#### WidgetCustomRepo

For fast access to individual widgets I decided on having a HashMap to widget's reference of whatever underlying structure
that's going to hold all widgets. O(1)
For checking existing vs non existing zIndexes also used another HashMap. (This also helped in finding 
the right place to add the new element to faster, for more check binarySearch part in code with comments)

For main data structure to hold all widgets , I was thinking at the beginning of using a sorted data structure based on zIndex,
In all cases writing a widget with existing z-index will need in the worst case O(N) iterations no matter which data structure 
I'm using because of shifting (If you know a magical way of doing it faster I'll be happy to know it :D), 
Main candidate was TreeSet which will be Olog(n) for both reading individual widgets 
and writing a new zIndex widget, I even thought about creating my own DoublyLinkedList with hash tables also in place
which will make it O(1) for deletion as we have the zIndex map to nodes.
I chose ArrayList in the end with maintaining the right order mainly favoring it to TreeMap/Set to have the O(1) for findById and 
O(n) for findAll, and favoring it to doubly linked list for simplicity sacrificing the faster deletes.

Important note: all references are maintained internally and whenever any method is called 
I copy the values of it and always return a copy of the widget back so that no accidental changes through references occur.

#### Locking
It's obvious that handling the concurrency can be done in multiple ways, (Synchronized, RWlock, StampedLock,...etc). 
Personally if I have this task at work I'll go first with using the simple Synchronized functions as the benchmarks 
already show that it's good enough for multiple cases, and then by checking metrics and visiting behaviors I'll start 
adding other locking ways if the performance can get better and do some technical A/B experiments to determine 
the performance on a larger scale, but for the sake of this problem after reviewing some benchmarks it seems that
with a system like this the best type of locks to use is the StampedLock with optimistic reading, the RW lock can
cause starvation and even the fair mode is much slower than StampedLock. 
- For reading methods I used the optimistic read as it showed better run times in benchmarks for most cases especially for 
heavy reading systems.

### Architecture notes
I'm implementing the business logic (shifting) in the custom repo. This is not ideal when adding more infrastructure layers (e.g. H2)
Ideally I want to have one service doing the logic and both repos implementing methods needed to do it. So something like 
the `WidgetH2Service` will be the only one doing the shifting and updating, and both repos will implement the methods accordingly
just as data layer.
An alternative for using Locking in the service is to use @Transactional to attempt Isolation and Atomicity, though this
won't work out of the box with the custom data layer, so I left it with locks keeping in mind that this  service will 
call any repo(H2/Custom) based on the properties file.

### About complications
- Pagination is implemented
- You can change data and service to use from `application.properties`.
    - For Custom in memory use: `service.type=customService` and `storage.type=custom`
    - For H2 use: `service.type=h2Service` and `storage.type=h2`
- Filtering: You asked for less than O(n), the naive approach to iterate on them is O(n) but I didn't implement it as that's
not what you're asking for. One Idea I had is to have hash table with all points in Plane and which objects lie before them
but this is using heavy memory. I think maybe using SegmentTrees can do the trick here.
- For Rate limiting I wanted to use Redis and background thread that runs every time unit (minute) to reset the counters for
global and local endpoints and have RequestFilter that checks for available count and decrement it or return `429` with proper headers.
 


### Final Notes
It was fun working on this task. Thanks =).
