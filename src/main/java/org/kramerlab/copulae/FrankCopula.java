package org.kramerlab.copulae;

import ch.visnet.jgsl.Jgsl;
import ch.visnet.jgsl.sf.Debye;
import org.kramerlab.vines.Utils;

/**
 * This is a placeholder for the Frank copula family.
 * It is not implemented yet.
 * 
 * @author Christian Lamberty (clamber@students.uni-mainz.de)
 */
public class FrankCopula extends AbstractCopula{
	double d;
	
	/**
	 * Constructor
	 * @param params copula parameters as double array.
	 */
	public FrankCopula(double[] params) {
		super(params);
		d = params[0];
	}

	@Override
	public double density(double x, double y) {
		// TODO Use Wavelets to solve
		return 0;
	}
	
	@Override
	public double hFunction(double x, double y) {
		x = Utils.laplaceCorrection(x);
		y = Utils.laplaceCorrection(y);
		
		double out = Math.exp(-d*y)/(Math.exp(-d*y)-1+
						(1-Math.exp(-d))/(1-Math.exp(-d*x)));
		
		return out;
	}
	
	@Override
	public double inverseHFunction(double x, double y) {
		x = Utils.laplaceCorrection(x);
		y = Utils.laplaceCorrection(y);
		
		double out = -Math.log(1-(1-Math.exp(-d))
				/(1+Math.exp(-d*y)*(1/x-1)))/d;
		
		return out;
	}
	
	@Override
	public double tau() {
		Jgsl.init();
		return 1-4/d+4*Debye.debye1(d)/d;
	}

	@Override
	public String name() {
		return "Frank";
	}
}