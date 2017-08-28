/**
 * 
 */
package edu.illinois.cs.cogcomp.utils;

/**
 * @author vivek
 *
 */
public class ExecutionTimeUtil
{
	long	_start;

	long _end;
	
	boolean started;
	
	public ExecutionTimeUtil()
	{
		reset();
	}
	
	public void start()
	{
		_start = System.currentTimeMillis();
		_end = 0;
		started = true;
	}
	
	public void end()
	{
		_end = System.currentTimeMillis();
		started = false;
	}
	
	public long getTimeMillis()
	{
		if(!started)
		{
			return (_end - _start);
		}
		else
			throw new IllegalStateException("Timer not ended");
	}
	
	public double getTimeSeconds() {
		if(!started)
		{
			return (_end - _start)/(double)1000;
		}
		else
			throw new IllegalStateException("Timer not ended");
	}

	/**
	 * 
	 */
	public void reset()
	{
		_start =0;
		_end = 0;
		started = false;
		
	}
}
