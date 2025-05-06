ModelEvaluator evaluator = new ModelEvaluator("models/deep_cnn_scripted.pt");

float[] state = encodeStateFromGame(game); // 4x9x9 --> {O,K,W,B}x9x9
float score = evaluator.evaluate(state);

System.out.println("Valutazione stato: " + score);

evaluator.close();
