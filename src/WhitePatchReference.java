import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.core.Core.MinMaxLocResult;
import org.opencv.videoio.VideoCapture;
import org.opencv.highgui.HighGui;

import java.util.ArrayList;
import java.util.List;

public class WhitePatchReference {

    public static Mat whitePatchReference(Mat frame) {
        // Separate color channels
        List<Mat> channelsList = new ArrayList<>();
        Core.split(frame, channelsList);

        // Calculate scaling factors for each channel
        Scalar scalingFactors = calculateScalingFactors(channelsList);

        // Apply white balancing to each channel
        for (Mat channel : channelsList) {
            // Scale the channel using the calculated factor
            Core.multiply(channel, scalingFactors, channel);
        }

        // Merge the channels back into the BGR image
        Mat balancedFrame = new Mat();
        Core.merge(channelsList, balancedFrame);

        // Convert the balanced frame to an 8-bit unsigned integer image
        balancedFrame.convertTo(balancedFrame, CvType.CV_8U);

        return balancedFrame;
    }

    private static Scalar calculateScalingFactors(List<Mat> channelsList) {
        // Initialize scaling factors
        Scalar scalingFactors = new Scalar(1.0, 1.0, 1.0);

        // Find the maximum intensity in each channel
        for (int i = 0; i < channelsList.size(); i++) {
            MinMaxLocResult minMaxResult = Core.minMaxLoc(channelsList.get(i));
            double maxVal = minMaxResult.maxVal;

            // Set the scaling factor for the channel
            scalingFactors.val[i] = 255.0 / maxVal;
        }

        return scalingFactors;
    }

    public static void main(String[] args) {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

        VideoCapture vid = new VideoCapture(0);
        if (!vid.isOpened()) {
            System.out.println("Error opening video");
            return;
        }

        HighGui.namedWindow("Original");
        HighGui.namedWindow("Balanced");

        while (true) {
            Mat frame = new Mat();
            boolean ret = vid.read(frame);
            if (!ret || frame.empty()) {
                System.out.println("Error reading frame");
                break;
            }

            Mat balancedFrame = whitePatchReference(frame);

            HighGui.imshow("Original", frame);
            HighGui.imshow("Balanced", balancedFrame);

            int key = HighGui.waitKey(1);
            if (key == 'q' || key == 'Q') {
                break;
            }
        }
        vid.release();
        HighGui.destroyAllWindows();
    }
}
