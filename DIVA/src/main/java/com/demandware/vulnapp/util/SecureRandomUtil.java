package com.demandware.vulnapp.util;

import java.security.SecureRandom;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * The only real security implementation in this challenge, SecureRandomUtil manages a SecureRandom instance
 * and dispatches to it for requested values. The managed instance reseeds every so often 
 * 
 * @author Chris Smith
 *
 */
public class SecureRandomUtil
{
	private static final Integer COUNTDOWN_MAX = 1000000; //1 million uses
	private static final Integer SEED_SIZE = 1024;

	private final SecureRandom random;
	private final AtomicInteger countdown; //when countdown reaches 0, reseed and reset countdown

	private static SecureRandomUtil instance = new SecureRandomUtil();

	private SecureRandomUtil(){
		this.random = new SecureRandom();
		generateNewSeed();
		countdown = new AtomicInteger(COUNTDOWN_MAX);
	}

	private SecureRandom getRandom(){
		return this.random;
	}

	/**
	 * Reset the seed to a newly seeded value instead of constructing a new object.
	 * This is cryptographically strong.
	 */
	private void generateNewSeed(){
		this.random.setSeed( this.random.generateSeed( SEED_SIZE ) );
	}

	/**
	 * After some number of uses of this instance, reseed the value
	 * <br/>
	 * Note: this method is thread-safe-enough. Several threads can call it simultaneously,
	 * and more than one may reseed (which is thread-safe). Therefore, the countdown is a good-enough
	 * pseudo-lock to lower the likelihood of doing this operation twice, even though there are no major
	 * downsides to doing the operation several times.
	 */
	private void checkReseed(){
		if(this.countdown.decrementAndGet() <= 0)
		{
			this.countdown.set( COUNTDOWN_MAX );
			generateNewSeed();
		}
	}

	/**
	 * Calls managed SecureRandom method
	 * @see SecureRandom#nextBoolean()
	 */
	public static boolean nextBoolean()
	{
		instance.checkReseed();
		return instance.getRandom().nextBoolean();
	}

	/**
	 * Calls managed SecureRandom method
	 * @see SecureRandom#nextBytes(byte[])
	 */
	public static void nextBytes( byte[] bytes )
	{
		instance.checkReseed();
		instance.getRandom().nextBytes( bytes );
	}

	/**
	 * Calls managed SecureRandom method
	 * @see SecureRandom#nextDouble()
	 */
	public static double nextDouble()
	{
		instance.checkReseed();
		return instance.getRandom().nextDouble();
	}

	/**
	 * Calls managed SecureRandom method
	 * @see SecureRandom#nextFloat()
	 */
	public static float nextFloat()
	{
		instance.checkReseed();
		return instance.getRandom().nextFloat();
	}

	/**
	 * Calls managed SecureRandom method
	 * @see SecureRandom#nextGaussian()
	 */
	public static double nextGaussian()
	{
		instance.checkReseed();
		return instance.getRandom().nextGaussian();
	}

	/**
	 * Calls managed SecureRandom method
	 * @see SecureRandom#nextInt()
	 */
	public static int nextInt()
	{
		instance.checkReseed();
		return instance.getRandom().nextInt();
	}

	/**
	 * Calls managed SecureRandom method
	 * @see SecureRandom#nextInt(int)
	 */
	public static int nextInt( int bound )
	{
		instance.checkReseed();
		return instance.getRandom().nextInt( bound );
	}

	/**
	 * Calls managed SecureRandom method
	 * @see SecureRandom#nextLong()
	 */
	public static long nextLong()
	{
		instance.checkReseed();
		return instance.getRandom().nextLong();
	}

	/**
	 * return a Hex String containing size bytes e.g. size = 2 returned string length = 4
	 */
	public static String generateRandomHexString(int size){
		byte[] bytes = new byte[size];
		SecureRandomUtil.nextBytes(bytes);

		StringBuilder sb = new StringBuilder(size*2);
		for (int i = 0; i < size; i++){
			String theHex = Integer.toHexString(bytes[i] & 0xFF).toUpperCase();
			sb.append(theHex.length() == 1 ? "0" + theHex : theHex);
		}
		return sb.toString();
	}
}
