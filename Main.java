import java.util.*;
import java.util.concurrent.atomic.*;
import java.util.regex.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class Main {
    public static void main(String[] args) {
        System.out.println("Uber LLD implementations — pick a class and run its demo.");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Q1  Parking Lot (Multithreaded)   Companies: Amazon, Microsoft, Goldman Sachs, Uber
    // ─────────────────────────────────────────────────────────────────────────
    static class ParkingResult {
        public final int status;
        public final String spotId, vehicleNumber, ticketId;
        ParkingResult(int s, String sp, String v, String t) { status=s; spotId=sp; vehicleNumber=v; ticketId=t; }
    }

    static class ParkingLot {
        private final int[][][] spotType, spotActive;
        private final boolean[][][] occupied;
        private final int floors, rows, cols;
        private final Object lock = new Object();
        private final Map<String,String> vehicleToSpot = new HashMap<>(), ticketToSpot = new HashMap<>(),
                                         spotToVehicle = new HashMap<>(), spotToTicket = new HashMap<>();

        ParkingLot(String[][][] parking) {
            floors=parking.length; rows=parking[0].length; cols=parking[0][0].length;
            spotType=new int[floors][rows][cols]; spotActive=new int[floors][rows][cols];
            occupied=new boolean[floors][rows][cols];
            for (int f=0;f<floors;f++) for (int r=0;r<rows;r++) for (int c=0;c<cols;c++) {
                String[] p=parking[f][r][c].split("-");
                spotType[f][r][c]=Integer.parseInt(p[0]); spotActive[f][r][c]=Integer.parseInt(p[1]);
            }
        }
        ParkingResult park(int vType, String vNum, String ticketId) {
            synchronized(lock) {
                for (int f=0;f<floors;f++) for (int r=0;r<rows;r++) for (int c=0;c<cols;c++)
                    if (spotActive[f][r][c]==1 && spotType[f][r][c]==vType && !occupied[f][r][c]) {
                        occupied[f][r][c]=true; String sid=f+"-"+r+"-"+c;
                        vehicleToSpot.put(vNum,sid); ticketToSpot.put(ticketId,sid);
                        spotToVehicle.put(sid,vNum); spotToTicket.put(sid,ticketId);
                        return new ParkingResult(201,sid,vNum,ticketId);
                    }
                return new ParkingResult(404,"",vNum,ticketId);
            }
        }
        int removeVehicle(String spotId, String vNum, String ticketId) {
            synchronized(lock) {
                String s=!spotId.isEmpty()?spotId:!vNum.isEmpty()?vehicleToSpot.get(vNum):ticketToSpot.get(ticketId);
                if (s==null) return 404;
                String[] p=s.split("-"); int f=Integer.parseInt(p[0]),r=Integer.parseInt(p[1]),c=Integer.parseInt(p[2]);
                if (!occupied[f][r][c]) return 404;
                occupied[f][r][c]=false; return 201;
            }
        }
        ParkingResult searchVehicle(String spotId, String vNum, String ticketId) {
            String s; String v=vNum!=null?vNum:"", t=ticketId!=null?ticketId:"";
            if (!spotId.isEmpty()) { s=spotId; v=spotToVehicle.getOrDefault(s,""); t=spotToTicket.getOrDefault(s,""); }
            else if (!vNum.isEmpty()) { s=vehicleToSpot.get(vNum); if(s!=null) t=spotToTicket.getOrDefault(s,""); }
            else { s=ticketToSpot.get(ticketId); if(s!=null) v=spotToVehicle.getOrDefault(s,""); }
            return s==null?new ParkingResult(404,"",v,t):new ParkingResult(201,s,v,t);
        }
        int getFreeSpotsCount(int floor, int vType) {
            synchronized(lock) {
                int cnt=0;
                for (int r=0;r<rows;r++) for (int c=0;c<cols;c++)
                    if (spotActive[floor][r][c]==1 && spotType[floor][r][c]==vType && !occupied[floor][r][c]) cnt++;
                return cnt;
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Q5  Food Ordering   Companies: Uber, Flipkart, ClearTax
    // ─────────────────────────────────────────────────────────────────────────
    static class FoodOrderingService {
        private final Map<String,String[]> orders=new HashMap<>();
        private final Map<String,int[]> restRatings=new HashMap<>(), foodRatings=new HashMap<>();
        private final Set<String> restaurants=new LinkedHashSet<>();
        private final Map<String,Set<String>> foodRests=new HashMap<>();

        void orderFood(String orderId, String restId, String foodId) {
            orders.put(orderId,new String[]{restId,foodId}); restaurants.add(restId);
            restRatings.putIfAbsent(restId,new int[2]);
            foodRatings.putIfAbsent(restId+"|"+foodId,new int[2]);
            foodRests.computeIfAbsent(foodId,k->new LinkedHashSet<>()).add(restId);
        }
        void rateOrder(String orderId, int rating) {
            String[] info=orders.get(orderId);
            int[] rr=restRatings.get(info[0]); rr[0]+=rating; rr[1]++;
            int[] fr=foodRatings.get(info[0]+"|"+info[1]); fr[0]+=rating; fr[1]++;
        }
        List<String> getTopRestaurantsByFood(String foodId) {
            List<String> list=new ArrayList<>(foodRests.getOrDefault(foodId,Collections.emptySet()));
            list.sort((a,b)->{ double ra=avg(foodRatings,a+"|"+foodId),rb=avg(foodRatings,b+"|"+foodId); return ra!=rb?Double.compare(rb,ra):a.compareTo(b); });
            return list.subList(0,Math.min(20,list.size()));
        }
        List<String> getTopRatedRestaurants() {
            List<String> list=new ArrayList<>(restaurants);
            list.sort((a,b)->{ double ra=avg(restRatings,a),rb=avg(restRatings,b); return ra!=rb?Double.compare(rb,ra):a.compareTo(b); });
            return list.subList(0,Math.min(20,list.size()));
        }
        private double avg(Map<String,int[]> m, String k) {
            int[] d=m.getOrDefault(k,new int[2]); if(d[1]==0) return 0;
            double raw=(double)d[0]/d[1]; return (double)((int)((raw+0.05)*10))/10.0;
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Q6  Hit Counter (Multithreaded)   Companies: Amazon, Adobe, Atlassian
    // ─────────────────────────────────────────────────────────────────────────
    static class WebpageVisitCounter {
        private final AtomicIntegerArray counts;
        WebpageVisitCounter(int totalPages) { counts=new AtomicIntegerArray(totalPages); }
        void incrementVisitCount(int pageIndex) { counts.incrementAndGet(pageIndex); }
        int getVisitCount(int pageIndex) { return counts.get(pageIndex); }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Q10  Movie Ticket Booking   Companies: Salesforce, Microsoft, Uber
    // ─────────────────────────────────────────────────────────────────────────
    static class MovieTicketService {
        private static class Cinema {
            final int cityId; final boolean[][][] screens;
            Cinema(int cityId,int sc,int rows,int cols) {
                this.cityId=cityId; screens=new boolean[sc][rows][cols];
                for (boolean[][] s:screens) for (boolean[] r:s) Arrays.fill(r,true);
            }
        }
        private static class Show {
            final int movieId,cinemaId,screenIdx; final long startTime;
            Show(int m,int c,int si,long st) { movieId=m; cinemaId=c; screenIdx=si; startTime=st; }
        }
        private static class Ticket {
            final int showId; final List<int[]> seats; boolean cancelled;
            Ticket(int s,List<int[]> seats) { showId=s; this.seats=seats; }
        }
        private final Map<Integer,Cinema> cinemas=new HashMap<>();
        private final Map<Integer,Show> shows=new HashMap<>();
        private final Map<String,Ticket> tickets=new HashMap<>();

        void addCinema(int cId,int cityId,int sc,int rows,int cols) { cinemas.put(cId,new Cinema(cityId,sc,rows,cols)); }
        void addShow(int sId,int mId,int cId,int screenIdx,long start,long end) {
            Cinema c=cinemas.get(cId); int si=screenIdx-1;
            shows.put(sId,new Show(mId,cId,si,start));
        }
        List<String> bookTicket(String ticketId,int showId,int count) {
            Show show=shows.get(showId); if(show==null) return Collections.emptyList();
            boolean[][] screen=cinemas.get(show.cinemaId).screens[show.screenIdx];
            int rows=screen.length,cols=screen[0].length;
            for (int r=0;r<rows;r++) for (int c=0;c<=cols-count;c++) {
                boolean ok=true; for(int k=0;k<count;k++) if(!screen[r][c+k]){ok=false;break;}
                if(ok) { List<int[]> seats=new ArrayList<>(); for(int k=0;k<count;k++){screen[r][c+k]=false;seats.add(new int[]{r,c+k});}
                    tickets.put(ticketId,new Ticket(showId,seats)); return fmt(seats); }
            }
            List<int[]> avail=new ArrayList<>();
            for(int r=0;r<rows;r++) for(int c=0;c<cols;c++) if(screen[r][c]) avail.add(new int[]{r,c});
            if(avail.size()<count) return Collections.emptyList();
            List<int[]> seats=avail.subList(0,count); for(int[] s:seats) screen[s[0]][s[1]]=false;
            tickets.put(ticketId,new Ticket(showId,new ArrayList<>(seats))); return fmt(seats);
        }
        private List<String> fmt(List<int[]> s) { List<String> r=new ArrayList<>(); for(int[] a:s) r.add(a[0]+"-"+a[1]); return r; }
        boolean cancelTicket(String ticketId) {
            Ticket t=tickets.get(ticketId); if(t==null||t.cancelled) return false;
            t.cancelled=true; Show show=shows.get(t.showId); boolean[][] sc=cinemas.get(show.cinemaId).screens[show.screenIdx];
            for(int[] s:t.seats) sc[s[0]][s[1]]=true; return true;
        }
        int getFreeSeatsCount(int showId) {
            Show show=shows.get(showId); if(show==null) return 0;
            boolean[][] sc=cinemas.get(show.cinemaId).screens[show.screenIdx];
            int cnt=0; for(boolean[] row:sc) for(boolean seat:row) if(seat) cnt++; return cnt;
        }
        List<Integer> listCinemas(int movieId,int cityId) {
            Set<Integer> r=new TreeSet<>();
            for(Show s:shows.values()) if(s.movieId==movieId&&cinemas.get(s.cinemaId).cityId==cityId) r.add(s.cinemaId);
            return new ArrayList<>(r);
        }
        List<Integer> listShows(int movieId,int cinemaId) {
            List<Map.Entry<Integer,Show>> e=new ArrayList<>();
            for(Map.Entry<Integer,Show> en:shows.entrySet()) if(en.getValue().movieId==movieId&&en.getValue().cinemaId==cinemaId) e.add(en);
            e.sort((a,b)->{ int c=Long.compare(b.getValue().startTime,a.getValue().startTime); return c!=0?c:Integer.compare(a.getKey(),b.getKey()); });
            List<Integer> r=new ArrayList<>(); for(Map.Entry<Integer,Show> en:e) r.add(en.getKey()); return r;
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Q21  Car Rental   Companies: Amazon, Uber, Flipkart
    // ─────────────────────────────────────────────────────────────────────────
    static class CarRentalService {
        private static class Car { final int cpd,fkpd,cpk; Car(int a,int b,int c){cpd=a;fkpd=b;cpk=c;} }
        private static class Booking {
            final String plate; final LocalDate from,till; int startOdo; LocalDate actualEnd;
            Booking(String p,LocalDate f,LocalDate t){plate=p;from=f;till=t;}
            LocalDate blockedUntil(){return actualEnd!=null?actualEnd:till;}
        }
        private final Map<String,Car> cars=new HashMap<>();
        private final Map<String,Booking> bookings=new HashMap<>();

        void addCar(String plate,int cpd,int fkpd,int cpk){ cars.putIfAbsent(plate,new Car(cpd,fkpd,cpk)); }
        boolean bookCar(String orderId,String plate,String from,String till) {
            if(!cars.containsKey(plate)) return false;
            LocalDate f=LocalDate.parse(from),t=LocalDate.parse(till);
            for(Booking b:bookings.values()) if(b.plate.equals(plate)&&!f.isAfter(b.blockedUntil())&&!b.from.isAfter(t)) return false;
            bookings.put(orderId,new Booking(plate,f,t)); return true;
        }
        void startTrip(String orderId,int odo){ bookings.get(orderId).startOdo=odo; }
        int endTrip(String orderId,int finalOdo,String endDate) {
            Booking b=bookings.get(orderId); Car car=cars.get(b.plate);
            LocalDate end=LocalDate.parse(endDate); b.actualEnd=end;
            LocalDate eff=end.isAfter(b.till)?end:b.till;
            long days=1+ChronoUnit.DAYS.between(b.from,eff);
            int kms=finalOdo-b.startOdo; long free=days*car.fkpd;
            return (int)(days*car.cpd)+((int)Math.max(0,kms-free))*car.cpk;
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Q27  Text Editor   Companies: Microsoft, Google, Amazon, Uber
    // ─────────────────────────────────────────────────────────────────────────
    static class TextEditor {
        interface Command { void execute(List<StringBuilder> r); void undo(List<StringBuilder> r); }
        private static class AddText implements Command {
            final int row,col; final String text; final boolean newRow;
            AddText(int row,int col,String text,boolean newRow){this.row=row;this.col=col;this.text=text;this.newRow=newRow;}
            public void execute(List<StringBuilder> r){if(newRow)r.add(new StringBuilder());r.get(row).insert(col,text);}
            public void undo(List<StringBuilder> r){r.get(row).delete(col,col+text.length());if(newRow)r.remove(row);}
        }
        private static class DeleteText implements Command {
            final int row,col,length; String deleted;
            DeleteText(int row,int col,int length){this.row=row;this.col=col;this.length=length;}
            public void execute(List<StringBuilder> r){StringBuilder sb=r.get(row);deleted=sb.substring(col,col+length);sb.delete(col,col+length);}
            public void undo(List<StringBuilder> r){r.get(row).insert(col,deleted);}
        }
        private final List<StringBuilder> rows=new ArrayList<>();
        private final Deque<Command> undoStack=new ArrayDeque<>(), redoStack=new ArrayDeque<>();

        void addText(int row,int col,String text){boolean nr=(row==rows.size());Command cmd=new AddText(row,col,text,nr);cmd.execute(rows);undoStack.push(cmd);redoStack.clear();}
        void deleteText(int row,int col,int len){Command cmd=new DeleteText(row,col,len);cmd.execute(rows);undoStack.push(cmd);redoStack.clear();}
        void undo(){if(!undoStack.isEmpty()){Command c=undoStack.pop();c.undo(rows);redoStack.push(c);}}
        void redo(){if(!redoStack.isEmpty()){Command c=redoStack.pop();c.execute(rows);undoStack.push(c);}}
        String readLine(int row){return rows.get(row).toString();}
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Q29  Meeting Room Reservation   Companies: Uber
    // ─────────────────────────────────────────────────────────────────────────
    static class RoomBooking {
        private final List<String> rooms;
        private final Map<String,List<int[]>> roomBookings=new HashMap<>();
        private final Map<String,Object[]> meetings=new HashMap<>();

        RoomBooking(List<String> roomIds){
            rooms=new ArrayList<>(roomIds); Collections.sort(rooms);
            for(String r:rooms) roomBookings.put(r,new ArrayList<>());
        }
        String bookMeeting(String id,int start,int end){
            for(String room:rooms) if(avail(room,start,end)){roomBookings.get(room).add(new int[]{start,end});meetings.put(id,new Object[]{room,start,end});return room;}
            return "";
        }
        boolean cancelMeeting(String id){
            Object[] info=meetings.remove(id); if(info==null) return false;
            roomBookings.get((String)info[0]).removeIf(b->b[0]==(int)info[1]&&b[1]==(int)info[2]); return true;
        }
        private boolean avail(String room,int s,int e){for(int[] b:roomBookings.get(room)) if(s<=b[1]&&b[0]<=e) return false; return true;}
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Q30  Filesystem Wildcard   Companies: Uber, Atlassian
    // ─────────────────────────────────────────────────────────────────────────
    static class FileSystemWildcard {
        private static class FsNode {
            final String name; FsNode parent; final TreeMap<String,FsNode> children=new TreeMap<>();
            FsNode(String n,FsNode p){name=n;parent=p;}
        }
        private final FsNode root; private FsNode current;
        FileSystemWildcard(){root=new FsNode("",null);root.parent=root;current=root;}
        String pwd(){if(current==root) return "/"; Deque<String> p=new ArrayDeque<>(); FsNode n=current; while(n!=root){p.addFirst(n.name);n=n.parent;} return "/"+String.join("/",p);}
        void mkdir(String path){FsNode base=path.startsWith("/")?root:current; for(String s:segs(path)){if(s.equals("."))continue;if(s.equals("..")){base=base.parent;continue;} final FsNode par=base; base=base.children.computeIfAbsent(s,k->new FsNode(k,par));}}
        void cd(String path){FsNode base=path.startsWith("/")?root:current; for(String s:segs(path)){if(s.equals("."))continue;else if(s.equals("..")){base=base.parent;}else if(s.equals("*")){if(!base.children.isEmpty())base=base.children.firstEntry().getValue();}else{FsNode n=base.children.get(s);if(n==null)return;base=n;}} current=base;}
        private List<String> segs(String p){List<String> r=new ArrayList<>();for(String s:p.split("/"))if(!s.isEmpty())r.add(s);return r;}
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Q31  Leaderboard   Companies: Uber, Microsoft, WayFair
    // ─────────────────────────────────────────────────────────────────────────
    static class Leaderboard {
        private final Map<String,List<String>> userPlayers=new HashMap<>();
        private final Map<String,Integer> playerScores=new HashMap<>();
        void addUser(String uid,List<String> players){userPlayers.put(uid,new ArrayList<>(players));for(String p:players)playerScores.putIfAbsent(p,0);}
        void addScore(String pid,int delta){playerScores.merge(pid,delta,Integer::sum);}
        List<String> getTopK(int k){List<String> users=new ArrayList<>(userPlayers.keySet());users.sort((a,b)->{int sa=score(a),sb=score(b);return sa!=sb?Integer.compare(sb,sa):a.compareTo(b);});return users.subList(0,Math.min(k,users.size()));}
        private int score(String uid){int t=0;for(String p:userPlayers.get(uid))t+=playerScores.getOrDefault(p,0);return t;}
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Q32  Train Platform Management   Companies: Uber
    // ─────────────────────────────────────────────────────────────────────────
    static class TrainPlatformManager {
        private static class Slot{final String trainId;final int start,end;Slot(String t,int s,int e){trainId=t;start=s;end=e;}}
        private final int n; private final int[] freeAt;
        @SuppressWarnings("unchecked") private final List<Slot>[] slots;
        private final Map<String,Slot> trainSlot=new HashMap<>();
        @SuppressWarnings("unchecked")
        TrainPlatformManager(int n){this.n=n;freeAt=new int[n];slots=new ArrayList[n];for(int i=0;i<n;i++)slots[i]=new ArrayList<>();}
        String assignPlatform(String trainId,int arrival,int wait){
            int best=0,bestDelay=Math.max(0,freeAt[0]-arrival);
            for(int i=1;i<n;i++){int d=Math.max(0,freeAt[i]-arrival);if(d<bestDelay){bestDelay=d;best=i;}}
            int start=arrival+bestDelay,end=start+wait; freeAt[best]=end;
            Slot s=new Slot(trainId,start,end); slots[best].add(s); trainSlot.put(trainId,s);
            return best+","+bestDelay;
        }
        String getTrainAtPlatform(int p,int t){for(Slot s:slots[p])if(t>=s.start&&t<s.end)return s.trainId;return "";}
        int getPlatformOfTrain(String trainId,int t){for(int i=0;i<n;i++)for(Slot s:slots[i])if(s.trainId.equals(trainId)&&t>=s.start&&t<s.end)return i;return -1;}
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Q33  PubSub System   Companies: Uber, InMobi, PayPal
    // ─────────────────────────────────────────────────────────────────────────
    static class PubSubService {
        private final Map<String,Set<String>> subs=new HashMap<>();
        private final Map<String,Integer> counts=new HashMap<>();
        void addSubscriber(String id,List<String> types){subs.put(id,new HashSet<>(types));counts.putIfAbsent(id,0);}
        void removeSubscriber(String id){subs.remove(id);}
        void sendMessage(String type,String msg){for(Map.Entry<String,Set<String>> e:subs.entrySet())if(e.getValue().contains(type))counts.merge(e.getKey(),1,Integer::sum);}
        int countProcessedMessages(String id){return counts.getOrDefault(id,0);}
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Q43  Custom HashMap   Companies: PayPal, Walmart, Flipkart, Uber
    // ─────────────────────────────────────────────────────────────────────────
    static class CustomHashMap {
        private final double minLF,maxLF; private int bucketsCount;
        @SuppressWarnings("unchecked") private List<String[]>[] buckets;
        private int size;
        @SuppressWarnings("unchecked")
        CustomHashMap(double min,double max){minLF=r2(min);maxLF=r2(max);bucketsCount=2;buckets=new ArrayList[2];for(int i=0;i<2;i++)buckets[i]=new ArrayList<>();}
        private double r2(double x){return Math.round(x*100.0)/100.0;}
        private int hash(String k){int len=k.length(),sum=0;for(char c:k.toCharArray())sum+=(c-'a'+1);return len*len+sum;}
        void put(String k,String v){int i=hash(k)%bucketsCount;for(String[] e:buckets[i])if(e[0].equals(k)){e[1]=v;return;}buckets[i].add(new String[]{k,v});size++;rehash();}
        String get(String k){for(String[] e:buckets[hash(k)%bucketsCount])if(e[0].equals(k))return e[1];return "";}
        String remove(String k){List<String[]> b=buckets[hash(k)%bucketsCount];for(int i=0;i<b.size();i++)if(b.get(i)[0].equals(k)){String v=b.get(i)[1];b.remove(i);size--;rehash();return v;}return "";}
        List<String> getBucketKeys(int i){if(i<0||i>=bucketsCount)return new ArrayList<>();List<String> r=new ArrayList<>();for(String[] e:buckets[i])r.add(e[0]);Collections.sort(r);return r;}
        int size(){return size;} int bucketsCount(){return bucketsCount;}
        @SuppressWarnings("unchecked") private void rehash(){
            double lf=r2((double)size/bucketsCount); int nc=bucketsCount;
            if(lf>maxLF){nc*=2;while(r2((double)size/nc)>maxLF)nc*=2;}
            else if(lf<minLF&&bucketsCount>2){nc=Math.max(2,bucketsCount/2);while(nc>2&&r2((double)size/nc)<minLF)nc/=2;}
            else return;
            List<String[]>[] nb=new ArrayList[nc];for(int i=0;i<nc;i++)nb[i]=new ArrayList<>();
            for(List<String[]> b:buckets)for(String[] e:b)nb[hash(e[0])%nc].add(e);
            buckets=nb;bucketsCount=nc;
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Q44  Meeting Room Scheduler   Companies: Salesforce, Uber
    // ─────────────────────────────────────────────────────────────────────────
    static class MeetingScheduler {
        private static class Booking{final String id;final int emp,room,start,end;Booking(String id,int e,int r,int s,int en){this.id=id;emp=e;room=r;start=s;end=en;}}
        private final int roomsCount;
        private final Map<String,Booking> bookings=new HashMap<>();
        private final Map<Integer,List<Booking>> byRoom=new HashMap<>(), byEmp=new HashMap<>();
        MeetingScheduler(int rooms,int emps){roomsCount=rooms;for(int i=0;i<rooms;i++)byRoom.put(i,new ArrayList<>());for(int i=0;i<emps;i++)byEmp.put(i,new ArrayList<>());}
        boolean bookRoom(String id,int emp,int room,int s,int e){
            if(s>e||s<0)return false;
            for(Booking b:byRoom.get(room))if(s<=b.end&&b.start<=e)return false;
            Booking b=new Booking(id,emp,room,s,e);bookings.put(id,b);byRoom.get(room).add(b);byEmp.get(emp).add(b);return true;
        }
        List<Integer> getAvailableRooms(int s,int e){if(s>e)return Collections.emptyList();List<Integer> r=new ArrayList<>();for(int i=0;i<roomsCount;i++){boolean free=true;for(Booking b:byRoom.get(i))if(s<=b.end&&b.start<=e){free=false;break;}if(free)r.add(i);}return r;}
        boolean cancelBooking(String id){Booking b=bookings.remove(id);if(b==null)return false;byRoom.get(b.room).remove(b);byEmp.get(b.emp).remove(b);return true;}
        List<String> listBookingsForRoom(int room){List<Booking> l=new ArrayList<>(byRoom.get(room));l.sort((a,b)->a.start!=b.start?Integer.compare(a.start,b.start):a.id.compareTo(b.id));List<String> r=new ArrayList<>();for(Booking b:l)r.add(b.id);return r;}
        List<String> listBookingsForEmployee(int emp){List<Booking> l=new ArrayList<>(byEmp.get(emp));l.sort((a,b)->a.start!=b.start?Integer.compare(a.start,b.start):a.id.compareTo(b.id));List<String> r=new ArrayList<>();for(Booking b:l)r.add(b.id);return r;}
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Q45  Meeting Room Recurrent   Companies: Salesforce, Uber
    // ─────────────────────────────────────────────────────────────────────────
    static class RecurrentMeetingScheduler {
        private static final int OCC=20;
        private static class RB{final String id;final int emp,room,s,dur,rep;RB(String id,int e,int r,int s,int d,int rp){this.id=id;emp=e;room=r;this.s=s;dur=d;rep=rp;}
            int os(int k){return s+k*rep;} int oe(int k){return os(k)+dur-1;}}
        private final int roomsCount;
        private final Map<String,RB> bookings=new HashMap<>();
        private final Map<Integer,List<RB>> byRoom=new HashMap<>(), byEmp=new HashMap<>();
        RecurrentMeetingScheduler(int rooms,int emps){roomsCount=rooms;for(int i=0;i<rooms;i++)byRoom.put(i,new ArrayList<>());for(int i=0;i<emps;i++)byEmp.put(i,new ArrayList<>());}
        boolean bookRoom(String id,int emp,int room,int s,int dur,int rep){
            if(s<0||dur<=0||dur>=rep)return false;
            RB nb=new RB(id,emp,room,s,dur,rep);
            for(RB ex:byRoom.get(room))for(int ki=0;ki<OCC;ki++)for(int kj=0;kj<OCC;kj++)if(nb.os(ki)<=ex.oe(kj)&&ex.os(kj)<=nb.oe(ki))return false;
            bookings.put(id,nb);byRoom.get(room).add(nb);byEmp.get(emp).add(nb);return true;
        }
        List<Integer> getAvailableRooms(int s,int e){if(s>e)return Collections.emptyList();List<Integer> r=new ArrayList<>();
            outer:for(int i=0;i<roomsCount;i++){for(RB b:byRoom.get(i))for(int k=0;k<OCC;k++)if(s<=b.oe(k)&&b.os(k)<=e)continue outer;r.add(i);}return r;}
        boolean cancelBooking(String id){RB b=bookings.remove(id);if(b==null)return false;byRoom.get(b.room).remove(b);byEmp.get(b.emp).remove(b);return true;}
        List<String> listBookingsForRoom(int room,int n){return listN(byRoom.get(room),n);}
        List<String> listBookingsForEmployee(int emp,int n){return listN(byEmp.get(emp),n);}
        private List<String> listN(List<RB> bList,int n){
            List<String[]> e=new ArrayList<>();for(RB b:bList)for(int k=0;k<OCC;k++)e.add(new String[]{b.id,String.valueOf(b.os(k)),String.valueOf(b.oe(k))});
            e.sort((a,b)->Integer.parseInt(a[1])!=Integer.parseInt(b[1])?Integer.compare(Integer.parseInt(a[1]),Integer.parseInt(b[1])):a[0].compareTo(b[0]));
            List<String> r=new ArrayList<>();for(int i=0;i<Math.min(n,e.size());i++){String[] s=e.get(i);r.add(s[0]+"-"+s[1]+"-"+s[2]);}return r;
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Q72  Kafka Streaming   Companies: Flipkart, ClearTrip, Uber
    // ─────────────────────────────────────────────────────────────────────────
    static class MessageStreamingService {
        private final Map<String,List<List<String>>> topics=new HashMap<>();
        private final Map<String,Integer> cursors=new HashMap<>();
        boolean createTopic(String name,int parts){if(topics.containsKey(name))return false;List<List<String>> p=new ArrayList<>();for(int i=0;i<parts;i++)p.add(new ArrayList<>());topics.put(name,p);return true;}
        String publish(String topic,int partition,String msg){List<String> p=topics.get(topic).get(partition);int off=p.size();p.add(msg);return "p"+partition+":"+off;}
        List<String> consume(String topic,String consumer,int partition,int max){
            String key=topic+"|"+consumer+"|"+partition; int cur=cursors.getOrDefault(key,0);
            List<String> p=topics.get(topic).get(partition); if(cur>=p.size())return Collections.emptyList();
            int end=Math.min(cur+max,p.size()); List<String> r=new ArrayList<>(p.subList(cur,end)); cursors.put(key,end); return r;
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Q192  Tic Tac Toe   Companies: Google, Amazon, Apple, Microsoft, Uber
    // ─────────────────────────────────────────────────────────────────────────
    static class TicTacToe {
        private final int n; private final int[][] rows,cols; private final int[] diag,antiDiag;
        TicTacToe(int n){this.n=n;rows=new int[2][n];cols=new int[2][n];diag=new int[2];antiDiag=new int[2];}
        int move(int row,int col,int player){
            int p=player-1; rows[p][row]++;cols[p][col]++;if(row==col)diag[p]++;if(row+col==n-1)antiDiag[p]++;
            return(rows[p][row]==n||cols[p][col]==n||diag[p]==n||antiDiag[p]==n)?player:0;
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Q202  Snake Game   Companies: Amazon, Google, Meta
    // ─────────────────────────────────────────────────────────────────────────
    static class SnakeGame {
        private final int width,height; private final int[][] food;
        private int foodIdx=0,score=0; private boolean over=false;
        private final Deque<int[]> snake=new ArrayDeque<>(); private final Set<String> body=new HashSet<>();
        SnakeGame(int w,int h,List<String> f){width=w;height=h;food=new int[f.size()][2];
            for(int i=0;i<f.size();i++){String[] p=f.get(i).split(",");food[i][0]=Integer.parseInt(p[0]);food[i][1]=Integer.parseInt(p[1]);}
            snake.addFirst(new int[]{0,0});body.add("0,0");}
        int move(String dir){if(over)return -1;int[] h=snake.peekFirst();int r=h[0],c=h[1];
            switch(dir){case"U":r--;break;case"D":r++;break;case"L":c--;break;default:c++;}
            if(r<0||r>=height||c<0||c>=width){over=true;return -1;}
            int[] tail=snake.pollLast();body.remove(tail[0]+","+tail[1]);
            if(body.contains(r+","+c)){over=true;return -1;}
            snake.addFirst(new int[]{r,c});body.add(r+","+c);
            if(foodIdx<food.length&&food[foodIdx][0]==r&&food[foodIdx][1]==c){snake.addLast(tail);body.add(tail[0]+","+tail[1]);score++;foodIdx++;}
            return score;}
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Q10362  Hit Counter   Companies: Google, Amazon, Adobe
    // ─────────────────────────────────────────────────────────────────────────
    static class ClickCounter {
        private final Deque<long[]> window=new ArrayDeque<>();
        void recordClick(int ts){if(!window.isEmpty()&&window.peekLast()[0]==ts)window.peekLast()[1]++;else window.addLast(new long[]{ts,1});}
        int getRecentClicks(int ts){int t=0;for(long[] e:window)if(e[0]>ts-300&&e[0]<=ts)t+=e[1];return t;}
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Terminal (In-Memory Filesystem)   Companies: Uber
    // ─────────────────────────────────────────────────────────────────────────
    static class FileSystem {
        private static class Node {
            final String name; final boolean isFile; Node parent; final Map<String,Node> children=new HashMap<>();
            Node(String n,boolean f,Node p){name=n;isFile=f;parent=p;}
        }
        private final Node root; private Node current;
        FileSystem(){root=new Node("/",false,null);root.parent=root;current=root;}
        String pwd(){if(current==root)return "/";List<String> p=new ArrayList<>();Node n=current;while(n!=root){p.add(n.name);n=n.parent;}Collections.reverse(p);return "/"+String.join("/",p);}
        String mkdir(String path){if(path==null||path.isEmpty())return"Invalid path";Node base=path.startsWith("/")?root:current;
            for(String part:path.split("/")){if(part.isEmpty()||part.equals("."))continue;if(part.equals("..")){base=base.parent;continue;}final Node par=base;base.children.computeIfAbsent(part,k->new Node(k,false,par));base=base.children.get(part);}return"";}
        String cd(String path){if(path==null||path.isEmpty())return"Invalid path";Node t=resolve(path);if(t==null)return"No such file or directory: "+path;if(t.isFile)return"Not a directory: "+path;current=t;return"";}
        String ls(String path){Node t=(path==null||path.isEmpty())?current:resolve(path);if(t==null)return"No such file or directory: "+path;if(t.isFile)return"[FILE] "+t.name;
            List<String> names=new ArrayList<>(t.children.keySet());Collections.sort(names);StringBuilder sb=new StringBuilder();
            for(int i=0;i<names.size();i++){Node c=t.children.get(names.get(i));if(i>0)sb.append("\n");sb.append(c.isFile?"[FILE] ":"[DIR] ").append(c.name);}return sb.toString();}
        String search(String regex){try{Pattern pat=Pattern.compile(regex);List<String> r=new ArrayList<>();dfs(current,pat,r,pwd());Collections.sort(r);return r.isEmpty()?"No matches found":String.join("\n",r);}catch(PatternSyntaxException e){return"Invalid regex: "+regex;}}
        private void dfs(Node node,Pattern pat,List<String> r,String nodePath){for(Node c:node.children.values()){String cp=nodePath.equals("/")?"/"+c.name:nodePath+"/"+c.name;if(pat.matcher(c.name).find())r.add(cp);if(!c.isFile)dfs(c,pat,r,cp);}}
        private Node resolve(String path){Node base=path.startsWith("/")?root:current;for(String p:path.split("/")){if(p.isEmpty()||p.equals("."))continue;if(p.equals("..")){base=base.parent;continue;}if(!base.children.containsKey(p))return null;base=base.children.get(p);}return base;}
    }
}
