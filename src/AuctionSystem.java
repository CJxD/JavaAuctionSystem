import com.cjwatts.auctionsystem.AuctionSystemClient;
import com.cjwatts.auctionsystem.AuctionSystemServer;

public class AuctionSystem {
	public static void main(String[] args) {
		// Set server mode if -server flag is set
		if (args.length >= 1) {
			if (args[0].equals("-server")) {
				AuctionSystemServer.main(args);
			} else {
				System.err.println("Invalid arguments. Use -server to run system as a server.");
				System.exit(1);
			}
		} else {
			AuctionSystemClient.main(args);
		}
	}
}
