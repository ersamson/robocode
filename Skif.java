package ers.nano.skif;

/**
 * Creator: ER Samson
 * 
 * Credits:
 * All Robocode developers and contributors at Robowiki!
 *
 * What's Different? 
 * MOVEMENT - A lightweight multimode table that has 3-major movement states
 * with a tweaked random oscillation logic by percentage so the behavior changes
 * when losing, hopefully to throw off learned targeters
 * 
 * Feel free to adopt, adapt and improve. Credits too when possible. :3
 */

import robocode.*;
import robocode.util.Utils;
//import java.awt.*;

public class Skif extends AdvancedRobot {

	/* ---------- Movement Variables ---------- */
	private static final int 		MV_FACTOR			= 3000;
	private static final int		T_DIST				= 180;
	private static double 		moveDir;
	private static double			enemyEnergy;
	private static int				moveMode;

	/* ---------- Gun Variables ---------- */
	private static final int		B_POWER				= 2; 	// (-1) asin offset below
	//private static final int		B_VELOCITY			= (int)(20 - 3 * B_POWER) - 1;
	private static final int		ANTIRAM_FACTOR		= 120;
	private static final int		D_HISTORY			= 32;
	private static double			accReversal;
	private static double			offSet;

	public void run() {
		// Color Identification
		//setColors(Color.darkGray, Color.gray, Color.orange);

		// Easy targetting and infinite radar movement
		setAdjustGunForRobotTurn(true); // Saving code size!
		setTurnRadarRightRadians(moveDir = Double.POSITIVE_INFINITY);
	}

	public void onScannedRobot(ScannedRobotEvent e) {
	
		// Variables	
		double absoluteBearing;
		double bulletPower;
		double lateralVelocity;


		/* ---------- Musashi Movement Logic ----------	*/
		// Turn perpendicular to enemy, maintain distance, no wall avoidance D:	
		// CREDIT: Yatagan			
		setTurnRightRadians(Math.cos((absoluteBearing = e.getBearingRadians()) + 
			((T_DIST - (e.getDistance())) * (getVelocity() / MV_FACTOR))));

		/* ---------- Gun Logic ---------- */
		// Save signum lateral velocities to compute for reversal bias
		accReversal += Math.signum(lateralVelocity = e.getVelocity() * Math.sin(e.getHeadingRadians() - 
			(absoluteBearing += getHeadingRadians())));

		// Linear targeting with Bullet power and speed calculator
		setTurnGunRightRadians(Utils.normalRelativeAngle(absoluteBearing + 
			(Math.abs(lateralVelocity) * offSet / Rules.getBulletSpeed(bulletPower = B_POWER + 
			(int)(ANTIRAM_FACTOR / e.getDistance()))) - getGunHeadingRadians()));
		
		// Fire with self-preservation
		if (getEnergy() > bulletPower) {
			setFire(bulletPower);
		}

		// Offset learning mechanism
		// Accumulrate signum of lateral velocities which determines reversals
		// Divide by number of ticks to generate reversal tendency ratio
		if (getTime() % D_HISTORY  == 0) {
			offSet = accReversal / D_HISTORY;
			accReversal = 0;		
		}


		/* ---------- Movement Logic ---------- */
		// Multimode movement - CREDIT: Yatagan, AralT, PraldeGuerre
		// -1 = Orbit, 0 = Energy Drop, various Random Oscillation
		setAhead(moveDir);
		
		if ((char)(MV_TABLE.charAt(moveMode) * Math.random() + 
			(enemyEnergy - 1 - (enemyEnergy = e.getEnergy()))) < 4) { 	
			onHitWall(null); // CREDIT: Simonton
		}


		/* ---------- Radar Logic ---------- */
		// From RoboWiki
		setTurnRadarLeft(getRadarTurnRemaining());		
	}


	/**
	 * Reverse direction when I hit wall
	 */
	public void onHitWall(HitWallEvent e) {
		moveDir = -moveDir;
	}


	/**
	 * On death, change movement strategy.
	 */
	public void onDeath(DeathEvent event) {
		moveMode++;
	}	


	/**
	 * Table constants (modified) - CREDIT: AralT
	 */
	private final static char  ORBIT	= (char) -1;	// One-Way Orbit
	private final static char  EDROP	= (char) 0;    // Stop-Go
	private final static char  OSC_1	= (char) 44;   // Random Oscillation ~9%
	private final static char  OSC_2	= (char) 33;   // Random Oscillation ~12%
	private final static char  OSC_3	= (char) 67;   // Random Oscillation ~6%
	
	private final static String MV_TABLE = "" + ORBIT + EDROP + ORBIT + EDROP 
			  + OSC_1 + OSC_1 + OSC_2 + OSC_2 + OSC_3 + OSC_3 + OSC_1 + OSC_1
			  + OSC_2 + OSC_2 + OSC_3 + OSC_3 + OSC_1 + OSC_1 + OSC_2 + OSC_2
			  + OSC_3 + OSC_3 + OSC_1 + OSC_1 + OSC_2 + OSC_2 + OSC_3 + OSC_3
			  + OSC_1 + OSC_1 + OSC_2 + OSC_2 + OSC_3 + OSC_3 + OSC_1 + OSC_1
		
			  // Overflow movements
			  + OSC_2 + OSC_2 + OSC_2 + OSC_3 + OSC_3 + OSC_1 + OSC_1 + OSC_1
			  + OSC_2 + OSC_2 + OSC_2 + OSC_3 + OSC_3 + OSC_1 + OSC_1 + OSC_1
			  + OSC_2 + OSC_2 + OSC_2 + OSC_3 + OSC_3 + OSC_1 + OSC_1 + OSC_1
			  + OSC_2 + OSC_2 + OSC_2 + OSC_3 + OSC_3 + OSC_1 + OSC_1 + OSC_1
			  + OSC_2 + OSC_2 + OSC_2 + OSC_3 + OSC_3 + OSC_1 + OSC_1 + OSC_1
			  + OSC_2 + OSC_2 + OSC_2 + OSC_3 + OSC_3 + OSC_1 + OSC_1 + OSC_1
			  + OSC_2 + OSC_2 + OSC_2 + OSC_3 + OSC_3 + OSC_1 + OSC_1 + OSC_1
			  + OSC_2 + OSC_2 + OSC_2 + OSC_3 + OSC_3 + OSC_1 + OSC_1 + OSC_1
			  + OSC_2 + OSC_2 + OSC_2 + OSC_3 + OSC_3 + OSC_1 + OSC_1 + OSC_1
			  + OSC_2 + EDROP + OSC_2 + OSC_2 + EDROP + ORBIT + EDROP + ORBIT;
}