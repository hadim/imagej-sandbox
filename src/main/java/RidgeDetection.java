import java.io.IOException;

import org.scijava.Context;
import org.scijava.convert.ConvertService;
import org.scijava.io.IOService;
import org.scijava.log.LogService;

import ij.ImagePlus;
import net.imagej.Dataset;
import net.imagej.DatasetService;
import net.imagej.ImageJ;
import net.imagej.ops.OpService;

public class RidgeDetection {

	public static void main(String args[]) throws IOException {

		final ImageJ ij = net.imagej.Main.launch(args);
		Context context = ij.getContext();

		LogService log = ij.log();
		OpService ops = ij.op();
		DatasetService ds = ij.dataset();
		IOService io = ij.io();
		ConvertService convert = ij.convert();

		String fpath = "/home/hadim/.doc/Code/Postdoc/ij/testdata/flat.tif";
		Dataset dataset = (Dataset) io.open(fpath);
		ij.ui().show(dataset);

		

		
	}

}