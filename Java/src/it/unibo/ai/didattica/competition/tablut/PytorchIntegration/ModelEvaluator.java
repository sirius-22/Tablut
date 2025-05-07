/*
 * import java.nio.file.Paths;
 * 
 * import ai.djl.inference.Predictor; import ai.djl.ndarray.*; import
 * ai.djl.translate.*;
 * 
 * public class ModelEvaluator { private Model model; private Predictor<NDArray,
 * NDArray> predictor; private NDManager manager;
 * 
 * public ModelEvaluator(String modelDir) throws Exception { manager =
 * NDManager.newBaseManager(); model = Model.newInstance("tablut-eval",
 * "PyTorch"); model.load(Paths.get(modelDir));
 * 
 * translator = new Translator<NDArray, NDArray>() { public NDArray
 * processInput(TranslatorContext ctx, NDArray input) { return input; }
 * 
 * public NDArray processOutput(TranslatorContext ctx, NDArray output) { return
 * output; }
 * 
 * public Batchifier getBatchifier() { return null; } };
 * 
 * predictor = model.newPredictor(translator); }
 * 
 * public float evaluate(float[] boardState) throws Exception { // boardState ha
 * shape (4x9x9) = 324 elementi NDArray input =
 * manager.create(boardState).reshape(new Shape(1, 4, 9, 9)); NDArray output =
 * predictor.predict(input); return output.toFloatArray()[0]; }
 * 
 * public void close() { predictor.close(); model.close(); manager.close(); } }
 */