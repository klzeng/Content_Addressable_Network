import java.net.InetAddress;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.LinkedList;
import java.util.Scanner;

public class Join {
    public static void main(String args[]){
        if(System.getSecurityManager() == null){
            System.setSecurityManager(new SecurityManager());
        }

        if(args.length != 0){
            String hostName = args[0];
            try{
                String name = "Node";
                Registry registry = LocateRegistry.getRegistry("Zengs-Macbook.fios-router.home");
                Node node = (Node) registry.lookup(name);
                InetAddress hostIP = InetAddress.getByName(hostName);
                String IP = hostIP.getHostAddress();

                float x = (float) (Math.random()*10);
                float y = (float) (Math.random()*10);

                LinkedList<String> path2join = new LinkedList<String>();
                LinkedList<String> joinInfo = node.join(hostName, new float[]{x,y}, path2join);

                if(joinInfo == null) {
                    System.out.println("Failure!");
                    System.exit(0);
                }

                System.out.println("\nbefore pooll:\n\n");
                for(String each: joinInfo){
                    System.out.println(each);
                }

                // set up the zone of the new node
                float xStart = Float.parseFloat(joinInfo.poll());
                float yStart = Float.parseFloat(joinInfo.poll());
                float xEnd = Float.parseFloat(joinInfo.poll());
                float yEnd = Float.parseFloat(joinInfo.poll());
                Point start =  new Point(xStart, yStart);
                Point end = new Point(xEnd, yEnd);
                Zone zone = new Zone(start, end);
                Peer peer = new Peer();
                peer.addZone(zone);
                peer.node.setIP(IP);
                peer.node.setName(hostName);

                System.out.println("\nafter pooll:\n\n");
                for(String each: joinInfo){
                    System.out.println(each);
                }


                // set up the neighbors of the new node
                while(!joinInfo.isEmpty()){
                    String neighbor = joinInfo.poll();
                    if(neighbor.contentEquals("KEYWORDS")) break;

                    System.out.println("\nadding neighbor: " + neighbor + "\n");

                    Registry registry1 = LocateRegistry.getRegistry(neighbor);
                    Node toAdd = (Node) registry1.lookup("Node");
                    LinkedList<float[]> zoneCoordnts = toAdd.getCoordnts();

                    String neighborIP = InetAddress.getByName(neighbor).getHostAddress();

                    Node_Base neighbor2Add = new Node_Base();
                    neighbor2Add.name = neighbor;
                    neighbor2Add.IP = neighborIP;
                    neighbor2Add.addZones(zoneCoordnts);
                    peer.neighbors.add(neighbor2Add);
                }

                // store the keywords belong to the new node
                while (!joinInfo.isEmpty()){
                    String keyword = joinInfo.poll();
                    peer.contents.add(keyword);
                }

                Node stub = (Node) UnicastRemoteObject.exportObject(peer, 0);
                Registry registry1 = LocateRegistry.getRegistry();
                registry1.rebind(name,stub);
                System.out.println("\nJoined!\n\n");
                System.out.println(peer.node.toString());

                Scanner scan = new Scanner(System.in);
                while (true){
                    System.out.print("> ");
                    String input = scan.nextLine();
                    String inputs[] = input.split(" ");
                    String cmd = inputs[0];
                    String arg = "";
                    if(inputs.length > 1){
                        arg = inputs[1];
                    }

                    String reply = "";
                    LinkedList<String> path = new LinkedList<String>();
                    if(cmd.contentEquals("insert")){
                        reply += "\nRoute to insert node:\n";
                        if(inputs.length ==2){
                            reply += peer.insert(arg, path);
                        }else {
                            inputs = input.split("\"");
                            if(inputs.length != 2){
                                System.out.println("usage: insert \"keyword\"");
                                continue;
                            }
                            reply += peer.insert(inputs[1], path);
                        }
                    }else if(cmd.contentEquals("view")){
                        if(arg.contentEquals("")){
                            LinkedList<String> viewed = new LinkedList<String>();
                            reply = peer.view(viewed);
                        }else {
                            reply = peer.view(arg);
                        }
                    }else if(cmd.contentEquals("leave")){
                        System.out.println("Issue leave on Bootstrap node make it unscalable(in my implementation).\n");
                        peer.leave();
                    }else if(cmd.contentEquals("search")){
                        reply += "\nSearching Route:";
                        if(inputs.length ==2){
                            reply = peer.search(arg, path);
                        }else {
                            inputs = input.split("\"");
                            if(inputs.length != 2){
                                System.out.println("usage: search \"keyword\"");
                                continue;
                            }
                            reply = peer.search(inputs[1], path);
                        }
                    }

                    System.out.println(reply);
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        else {
            try{
                String name = "Node";
                Registry registry = LocateRegistry.getRegistry(args[0]);
                Node bootNode = (Node) registry.lookup(name);
                Scanner scan = new Scanner(System.in);

            }catch (Exception e){
                e.printStackTrace();
            }
        }

    }
}
