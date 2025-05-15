package it.unibo.ai.didattica.competition.tablut.PytorchIntegration;

import java.nio.file.Paths;

import ai.djl.Device;
import ai.djl.Model;
import ai.djl.engine.Engine;
import ai.djl.inference.Predictor;
import ai.djl.ndarray.NDArray;
import ai.djl.ndarray.NDList;
import ai.djl.ndarray.NDManager;
import ai.djl.ndarray.types.Shape;
import ai.djl.pytorch.jni.JniUtils;
import ai.djl.translate.Batchifier;
import ai.djl.translate.Translator;
import ai.djl.translate.TranslatorContext;

public class ModelEvaluator {
	private String dirPath = "src/it/unibo/ai/didattica/competition/tablut/PytorchIntegration/models";
	private String fileName = "global_cnn_scripted_3000.pt";
	private String modelPath = dirPath + "/" + fileName;

	private Model model;
	private Predictor<NDArray, Float> predictor;
	private NDManager manager;
	private Translator<NDArray, Float> translator;
	private Engine engine;

	public ModelEvaluator() throws Exception {
		// System.out.println("BUILD: file is " + modelDir);
		Device device = Device.cpu();
		manager = NDManager.newBaseManager(device);
		engine = Engine.getEngine("PyTorch");
		JniUtils.setGraphExecutorOptimize(false);
		model = Model.newInstance("tablut-eval", device, "PyTorch");
		model.load(Paths.get(modelPath));

		translator = new Translator<NDArray, Float>() {

			// PreP -> NdList processInput(TranslatoContext, I)
			// PostP -> O processOutput(TranslatorContext, NDList)

			public NDList processInput(TranslatorContext ctx, NDArray input) {
				return new NDList(input);
			}

			@Override
			public Float processOutput(TranslatorContext ctx, NDList output) throws Exception {
				return output.get(0).toFloatArray()[0];
			}

			public Batchifier getBatchifier() {
				return null;
			}

		};

		predictor = model.newPredictor(translator);
	}

	public float evaluate(float[] boardState) throws Exception {
		// boardState ha shape (4x9x9) = 324 elementi
		NDArray input = manager.create(boardState).reshape(new Shape(1, 4, 9, 9));
		Float output = predictor.predict(input);
		return output;
	}

	public void close() {
		predictor.close();
		model.close();
		manager.close();
	}
}
