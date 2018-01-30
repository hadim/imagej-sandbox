import ij.ImagePlus;
import ij.process.ImageProcessor;

public class Utils {
	public static float[] getInterleave(ImagePlus imp) {

		int nz = 1;
		int ny = imp.getHeight();
		int nx = imp.getWidth();

		float[] interleave = new float[2 * nz * nx * ny];
		ImageProcessor ip = imp.getProcessor();

		for (int k = 0; k < nz; k++)
			for (int j = 0; j < ny; j++)
				for (int i = 0; i < nx; i++)
					interleave[2 * (j * ny + i)] = ip.getPixelValue(i, j);
		return interleave;
	}

	public static ImagePlus setInterleave(float[] interleave, ImagePlus source) {

		int nz = 1;
		int ny = source.getHeight();
		int nx = source.getWidth();

		ImageProcessor ip = source.getProcessor();

		for (int k = 0; k < nz; k++)
			for (int j = 0; j < ny; j++)
				for (int i = 0; i < nx; i++)
					ip.putPixelValue(i, j, interleave[(j * ny + i) * 2]);
		return source;
	}
}
