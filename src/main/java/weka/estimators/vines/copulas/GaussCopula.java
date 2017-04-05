package weka.estimators.vines.copulas;

import umontreal.ssj.probdist.NormalDist;
import umontreal.ssj.probdistmulti.BiNormalDist;
import weka.estimators.vines.Utils;

/**
 * This is the class to represent Gauss copula family for RVines.
 * <br>
 * The Kendall's tau calculation was presented by H. B. Fang, K. T. Fang and S. Kotz (2002):
 * The meta-elliptical distributions with given marginals.
 * <br>
 * The cumulative distribution function was presented in P.X.-K. Song (2000):
 * Multivariate dispersion models generated from gaussian copula.
 * <br>
 * The density function, the h-function and its inverse were
 * presented by K. Aas et al. (2009): Pair-copula constructions of
 * multiple dependence.
 * 
 * @author Christian Lamberty (clamber@students.uni-mainz.de)
 */
public class GaussCopula extends AbstractCopula{
	private double p;
	
	/**
	 * Constructor
	 * @param params parameter array, should be like:
	 * <br>
	 * params = {p}
	 * <br>
	 * p : probability | -1 &lt; p &lt; 1
	 */
	public GaussCopula(double[] params) {
		super(params);
		p = params[0];
		lb = -1+tol;
		ub = 1-tol;
		start = 0;
	}

	@Override
	public void setParams(double[] params){
		super.setParams(params);
		p = params[0];
	}

	public double C(double x, double y) {
		if(p==0) return x*y;
		
		x = Utils.laplaceCorrection(x);
		y = Utils.laplaceCorrection(y);
		
		double a = NormalDist.inverseF01(x);
		double b = NormalDist.inverseF01(y);
		
		return BiNormalDist.cdf(a, b, p);
	}
	
	public double density(double x, double y) {
		if(p==0) return 1;
		
		x = Utils.laplaceCorrection(x);
		y = Utils.laplaceCorrection(y);
		
		double a = NormalDist.inverseF01(x);
		double b = NormalDist.inverseF01(y);
		
		double pp = p*p;
		
		double out = Math.exp(-(pp*(a*a+b*b)-2*p*a*b) / (2*(1-pp)))
						/Math.sqrt(1-pp);
		
		return out;
	}

	public double h1Function(double x, double y) {
		if(p==0) return y;
		
		return hFunction(y, x);
	}

	public double h2Function(double x, double y) {
		if(p==0) return x;
		
		return hFunction(x, y);
	}
	
	/**
	 * H function for Gauss Copula.
	 * Since Gauss Copula is symmetric, we don't need
	 * separate h functions.
	 * @param x, y input parameters.
	 * @return returns the conditioned value x|y.
	 */
	public double hFunction(double x, double y) {
		x = Utils.laplaceCorrection(x);
		y = Utils.laplaceCorrection(y);
		
		double a = NormalDist.inverseF01(x);
		double b = NormalDist.inverseF01(y);
		
		double out = NormalDist.cdf01(
				( a-p*b ) / Math.sqrt(1-p*p) );
		
		return out;
	}
	
	public double tau(){
		return 2/Math.PI*Math.asin(p);
	}
	
	public String name() {
		return "G";
	}
	
	@Override
	public double[] getParBounds() {
		return new double[]{lb, ub};
	}
}