import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.scijava.Context;
import org.scijava.ItemIO;
import org.scijava.command.Command;
import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;
import org.scijava.ui.UIService;

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
	private UIService ui;

	@Parameter
	private Dataset data;

	@Parameter(type = ItemIO.OUTPUT)
	private Dataset output;

	@Parameter(type = ItemIO.OUTPUT)
	private Dataset junctionOutput;

	private List<DefaultPolyline> lines;
	private List<RealPoint> junctionPoints;

	@Override
	public void run() {

		// Parameters for ridge detection
		double width = 5;
		int ridgeLengthMin = 10;
		double lowThreshold = 5;
		double highThreshold = 10;

		boolean findJunctions = true;
		double junctionThreshold = 0;

		boolean printLines = true;

		// Get Img from Dataset
		Img<DoubleType> input = (Img<DoubleType>) data.getImgPlus().getImg();

		// Perform ridge detection
		lines = (List<DefaultPolyline>) op.run(RidgeDetection.class, input, width, lowThreshold, highThreshold,
				ridgeLengthMin);

		log.info(lines.size());

		// Create container for the binary image
		long[] dims = new long[input.numDimensions()];
		double[] sizes = new double[] { (double) data.getWidth(), (double) data.getHeight() };
		Img<DoubleType> output = ArrayImgs.doubles(sizes, dims);

		int xPos;
		int yPos;

		// Create binary image with detected lines
		for (DefaultPolyline line : lines) {

			if (printLines) {
				log.info(line.vertex(0));
				log.info(line.vertex(line.numVertices() - 1));
				log.info("------");
			}

			for (int i = 0; i < line.numVertices(); i++) {
				RealPoint p = line.vertex(i);
				xPos = (int) Math.round(p.getDoublePosition(0));
				yPos = (int) Math.round(p.getDoublePosition(1));
				output.randomAccess().get().setReal(255);
			}
		}
		
		if (findJunctions && lines.size() > 0) {
			// Perform junctions detection
			Img<DoubleType> junctionOutput = ArrayImgs.doubles(dims);

			junctionPoints = (List<RealPoint>) op.run(JunctionDetection.class, lines, junctionThreshold);
			log.info(junctionPoints.size());

			for (int i = 0; i < junctionPoints.size(); i++) {
				RealPoint p = junctionPoints.get(i);

				for (int x = -1; x <= 1; x++) {
					for (int y = -1; y <= 1; y++) {
						junctionOutput.randomAccess().setPosition(0, (int) Math.round(p.getDoublePosition(0) + x));
						junctionOutput.randomAccess().setPosition(1, (int) Math.round(p.getDoublePosition(1) + y));
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