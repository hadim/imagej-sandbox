import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.scijava.Context;
import org.scijava.ItemIO;
import org.scijava.command.Command;
import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;

import io.scif.services.DatasetIOService;
import net.imagej.Dataset;
import net.imagej.ImageJ;
import net.imagej.ops.OpService;
import net.imagej.ops.segment.ridgeDetection.JunctionDetection;
import net.imagej.ops.segment.ridgeDetection.RidgeDetection;
import net.imglib2.RealPoint;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.roi.geom.real.DefaultPolyline;
import net.imglib2.type.numeric.real.DoubleType;

public class RidgeDetect implements Command {

	@Parameter
	private LogService log;

	@Parameter
	private OpService op;

	@Parameter
	private Dataset data;

	@Parameter(type = ItemIO.OUTPUT)
	private List<DefaultPolyline> lines;

	@Parameter(type = ItemIO.OUTPUT)
	private Dataset output;

	@Parameter(type = ItemIO.OUTPUT)
	private Dataset junctionOutput;

	@Parameter(type = ItemIO.OUTPUT)
	private List<RealPoint> junctionPoints;

	@Override
	public void run() {

		// Parameters for ridge detection
		double width = 5;
		int ridgeLengthMin = 0;
		double lowThreshold = -555555555;
		double highThreshold = 255000000;

		boolean findJunctions = true;
		double junctionThreshold = 2;

		// Get Img from Dataset
		Img<DoubleType> input = (Img<DoubleType>) data.getImgPlus().getImg();

		// Perform ridge detection
		lines = (List<DefaultPolyline>) op.run(RidgeDetection.class, input, width, lowThreshold, highThreshold,
				ridgeLengthMin);

		log.info(lines);

		// Create container for the binary image
		long[] dims = new long[input.numDimensions()];
		Img<DoubleType> output = ArrayImgs.doubles(dims);

		// Create binary image with detected lines
		for (DefaultPolyline line : lines) {
			for (int i = 0; i < line.numVertices(); i++) {
				RealPoint p = line.vertex(i);
				dims[0] = (long) Math.round(p.getDoublePosition(0));
				dims[1] = (long) Math.round(p.getDoublePosition(1));
				output.randomAccess().setPosition(dims);
				output.randomAccess().get().setReal(255);
			}
		}

		if (findJunctions && lines.size() > 0) {
			// Perform junctions detection
			Img<DoubleType> junctionOutput = ArrayImgs.doubles(dims);

			junctionPoints = (List<RealPoint>) op.run(JunctionDetection.class, lines, junctionThreshold);

			for (int i = 0; i < junctionPoints.size(); i++) {
				RealPoint p = junctionPoints.get(i);

				for (int x = -1; x <= 1; x++) {
					for (int y = -1; y <= 1; y++) {

						dims[0] = (long) Math.round(p.getDoublePosition(0) + x);
						dims[1] = (long) Math.round(p.getDoublePosition(1) + y);
						junctionOutput.randomAccess().setPosition(dims);
						junctionOutput.randomAccess().get().setReal(255);
					}
				}
			}
		}

	}

	public static void main(String args[]) throws IOException {

		final ImageJ ij = net.imagej.Main.launch(args);
		Context context = ij.getContext();

		DatasetIOService dsio = (DatasetIOService) context.getService(DatasetIOService.class);

		String fpath = "/home/hadim/.doc/Code/Postdoc/ij/testdata/flat.tif";
		Dataset dataset = dsio.open(fpath);
		ij.ui().show(dataset);

		Map<String, Object> parameters = new HashMap<>();
		parameters.put("data", dataset);

		ij.command().run(RidgeDetect.class, true, parameters);
	}

}