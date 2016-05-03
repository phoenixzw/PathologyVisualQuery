package shapesketch;

import java.awt.Dimension;
import java.util.ArrayList;

import cluster.NewickNode;
import database.Point;
import database.ShapeData;
import processing.core.PApplet;

public class ClusterCanvas extends PApplet {
	ShapeData data;
	int canvasWidth;
	int canvasHeight;

	public ClusterCanvas(ShapeData data, Dimension dimension) {
		this.data = data;
		canvasWidth = dimension.width;
		canvasHeight = dimension.height;
	}

	public void setup() {
	}

	public void draw() {
		background(255);
		noFill();
		rect(0, 0, width - 1, height - 1);
		for (int i = 0; i < data.clusters.size(); i++) {
			pushMatrix();
			translate(canvasWidth * (i + 0.5f) / data.clusters.size(), canvasHeight / 2);
			drawAverage(data.clusters.get(i));
			popMatrix();
		}
	}

	private void drawAverage(NewickNode newickNode) {
		// TODO Auto-generated method stub
		fill(0);
		ArrayList<Point> sampleList = data.getResult().get(newickNode.indexList.get(0)).getSampleList();
		for (int i = 0; i < sampleList.size() - 1; i++) {
			line((float) sampleList.get(i).x, (float) sampleList.get(i).y, (float) sampleList.get(i + 1).x,
					(float) sampleList.get(i + 1).y);
		}
		line((float) sampleList.get(0).x, (float) sampleList.get(0).y, (float) sampleList.get(sampleList.size() - 1).x,
				(float) sampleList.get(sampleList.size() - 1).y);
	}

	public void mousePressed() {
		int index = (int) (mouseX * data.clusters.size() / canvasWidth);
		data.selectedCluster = index;
		System.out.println(data.selectedCluster);
	}
}
