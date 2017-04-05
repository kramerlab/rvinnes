package weka.estimators.vines.copulas;

import weka.estimators.vines.Utils;
import weka.estimators.vines.functions.CopulaMLE;
import weka.estimators.vines.functions.debyeSub;

/**
 * This is the class to represent Frank copula family for RVines.
 * <br>
 * The cumulative distribution function and the Kendall's tau calculation
 * were presented by M. Mahfoud and M. Michael (2012):
 * Bivariate archimedean copulas: an application to two stock market indices.
 * <br>
 * The density function, the h-function and its inverse were presented by
 * D. Schirmacher and E. Schirmacher (2008):
 * Multivariate dependence modeling using pair-copulas.
 * 
 * @author Christian Lamberty (clamber@students.uni-mainz.de)
 */
public class FrankCopula extends AbstractCopula{
	double d;
	
	/**
	 * Constructor
	 * @param params parameter array, should be like:
	 * <br>
	 * params = {d}
	 * <br>
	 * d : dependence | -infinity &lt; d &lt; infinity
	 */
	public FrankCopula(double[] params) {
		super(params);
		d = params[0];
		lb = -100;
		ub = 100;
		start = 0;
	}

	@Override
	public void setParams(double[] params){
		super.setParams(params);
		d = params[0];
	}
	
	@Override
	public double C(double x, double y) {
		if(d == 0) return x*y;
		
		x = Utils.laplaceCorrection(x);
		y = Utils.laplaceCorrection(y);
		
		double ed = Math.exp(-d)-1;
		double edx = Math.exp(-d*x)-1;
		double edy = Math.exp(-d*y)-1;
		
		return -Math.log(1+edx*edy/ed)/d;
	}
	
	@Override
	public double density(double x, double y) {
		if(d == 0) return 1;
		
		x = Utils.laplaceCorrection(x);
		y = Utils.laplaceCorrection(y);
		
		double ed = Math.exp(d);
		double edx = Math.exp(d*x);
		double edy = Math.exp(d*y);
		
		double z = d*ed*edx*edy*(ed-1);
		z = z/(ed*(1-edx+edx*edy/ed-edy));
		z = z/(ed*(1-edx+edx*edy/ed-edy));
		return z;
	}
	
	@Override
	public double h1Function(double x, double y) {
		return hFunction(y, x);
	}

	@Override
	public double h2Function(double x, double y) {
		return hFunction(x, y);
	}
	
	/**
	 * H function for Frank Copula.
	 * Since Frank Copula is symmetric, we don't need
	 * separate h functions.
	 * @param x, y input parameters.
	 * @return returns the conditioned value x|y.
	 */
	public double hFunction(double x, double y) {
		if(d == 0) return x;
		
		x = Utils.laplaceCorrection(x);
		y = Utils.laplaceCorrection(y);
		
		double ed = Math.exp(-d);
		double edx = Math.exp(-d*x);
		double edy = Math.exp(-d*y);
		
		double out = edy/(edy-1+
						(1-ed)/(1-edx));
		
		return out;
	}
	
	@Override
	public double tau() {
		if(d == 0) return 0;
		return 1 - 4 / d * (1 - debye1(d));
	}
	
	/**
	 * Debye1 function for tau calculation.
	 * @param x input parameter.
	 * @return returns the debye1(x) value.
	 */
	private double debye1(double x){
		if(x == 0) return 1;
		
		double y = 1;
		y = Utils.simpsonIntegrate(new debyeSub(), 1000, 0, x);
		
		return y/x;
	}
	
	@Override
	public String name() {
		return "F";
	}

	@Override
	public double[] getParBounds() {
		return new double[]{lb, ub};
	}
}