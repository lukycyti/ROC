package ProjetImageJ;

import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.process.ImageConverter;
import ij.process.ImageProcessor;

import java.io.File;
import java.util.Arrays;
import java.util.Vector;

/**
 * @author Clément / Lucas
 */
public class Main {

    static String path = "/mnt/roost/users/lbrouet/iut-bibliotheque/S4_Image/projetOCR/baseProjetOCR/";

    public static void main(String[] args) {
        new ImageJ();
        printMatrice(calculResult(listFiles(path)));
    }

    /**
     * Retourne un vecteur pour l'image passée en paramètre
     * @param ip ImageProcessor de l'image concernée
     * @return Le vecteur correspondant à l'image
     */
    private static Vector<Double> setVectorImage(ImageProcessor ip) {
        var v = new Vector<Double>();
        v.add(divideImage(ip));
        v.add(isoRelation(ip));
        v.add(nbContoursWidth(ip));
        v.add(nbContoursHeight(ip));
        return v;
    }

    /**
     * Retourne l'image la plus proche d'une autre image passée en paramètre
     * @param ip ImageProcessor d'une image
     * @param imagePath path d'une image
     * @return l'image la plus proche
     */
    public static ImagePlus closestImage(ImageProcessor ip, String imagePath) {
        File[] files = new File(path).listFiles();
        ImagePlus closestImage = null;
        if (files != null) {
            Vector<Double> v = setVectorImage(ip);
            double gap = Double.MAX_VALUE;
            for (File file : files)
                if (!file.isHidden() && !file.getAbsolutePath().equals(imagePath)) {
                    String filePath = file.getAbsolutePath();
                    ImagePlus tempImg = new ImagePlus(filePath);
                    new ImageConverter(tempImg).convertToGray8();
                    ImageProcessor ipTemp = tempImg.getProcessor();
                    Vector<Double> vTemp = setVectorImage(ipTemp);
                    Double dif = euclideanDistance(v, vTemp);
                    if (dif < gap) {
                        gap = dif;
                        closestImage = tempImg;
                    }
                }
        }
        return closestImage;
    }


    /**
     * Découpe une image en plusieurs parties
     * @param ip Processeur de l'image
     * @return le coeffeciant de la moyenne des niveaux de gris de chaque zone de l'image
     */
    private static Double divideImage(ImageProcessor ip) {
        float width = ip.getWidth();
        float height = ip.getHeight();
        double sum = 0;
        int occurence = 0;
        for (float x = 0; x < width; x = width / 3 + x) {
            for (float y = 0; y < height; y = height / 3 + y) {
                ip.setRoi((int) x, (int) y, ip.getWidth() / 3, ip.getHeight() / 3);
                double meanGray = meanImage(ip.crop());
                sum += meanGray;
                occurence++;
            }
        }
        return sum / occurence;
    }

    /**
     * Calcule le rapport isométrique d'une image
     * @param ip Processeur de l'image
     * @return le rapport isométrique de l'image
     */
    private static Double isoRelation(ImageProcessor ip) {
        int perimeter = (ip.getHeight() + ip.getWidth()) * 2;
        int surface = (ip.getHeight() * ip.getWidth());
        return perimeter / (4 * Math.PI * surface);
    }

    /**
     * Calcule le nombre de contours horizontaux
     * @param ip ImageProcessor de l'image
     * @return le nombre de contours de l'image
     */
    private static Double nbContoursWidth(ImageProcessor ip) {
        double nbContoursWidth = 0;
        for (int x = 0; x < ip.getWidth(); x++) {
            for (int y = 0; y < ip.getHeight(); y++) {
                int level = ip.getPixel(x, y);
                if (x != ip.getWidth() && ip.getPixel(x + 1, y) - level != 0) {
                    nbContoursWidth++;
                }
            }
        }
        return nbContoursWidth;
    }

    /**
     * Calcule le nombre de contours verticaux
     * @param ip ImageProcessor de l'image
     * @return le nombre de contours de l'image
     */
    private static Double nbContoursHeight(ImageProcessor ip) {
        double nbContoursHeight = 0;
        for (int x = 0; x < ip.getWidth(); x++) {
            for (int y = 0; y < ip.getHeight(); y++) {
                int level = ip.getPixel(x, y);
                if (y != ip.getHeight() && ip.getPixel(x, y + 1) - level != 0) {
                    nbContoursHeight++;
                }
            }
        }
        return nbContoursHeight;
    }

    /**
     * Calcule la distance euclidienne entre deux vecteurs
     *
     * @param v1 vecteur 1
     * @param v2 vecteur 2
     * @return la distance euclidienne entre v1 et v2
     */
    public static Double euclideanDistance(Vector<Double> v1, Vector<Double> v2) {
        double total = 0.0;
        for (int i = 0; i < v1.size(); i++) {
            total += Math.pow(Math.abs(v1.elementAt(i) - v2.elementAt(i)), 2);
        }
        return Math.sqrt(total);
    }

    /**
     * Méthode récupérant tous les fichiers d'un répertoire
     * @param directoryPath path du répertoire
     * @return un tableau de fichiers
     */
    public static File[] listFiles(String directoryPath) {
        File[] files;
        File directoryToScan = new File(directoryPath);
        files = directoryToScan.listFiles();
        return files;
    }

    /**
     * Calcul la moyenne du niveau de gris d'une image
     * @param ip ImageProcessor de l'image
     * @return la moyenne du niveau de gris de l'image
     */
    public static double meanImage(ImageProcessor ip) {
        int sum = 0;
        int[][] pixels = ip.getIntArray();
        for (int[] pixel : pixels) {
            for (int j : pixel) {
                sum += j;
            }
        }
        return (double) sum / ip.getPixelCount();
    }

    /**
     * Génère les résultats
     * @param files tableau de fichiers
     * @return un tableau
     */
    private static int[][] calculResult(File[] files) {
        int index = 0;
        int[][] result = createEmptyMatrice();
        for (int i = 0; i < result.length; i++) {
            for (int j = 0; j < result[i].length; j++) {
                if(index >= files.length) break;
                if (files[index].isHidden()) { index++; continue; }
                ImagePlus image = IJ.openImage(path + files[index].getName());
                new ImageConverter(image).convertToGray8();
                ImageProcessor ip = image.getProcessor();
                ImagePlus newImage = closestImage(ip, files[index].getAbsolutePath());
                int x = getIntegerImage(image);
                int y = getIntegerImage(newImage);
                result[x][y]++;
                index++;
            }
        }
        return result;
    }


    /**
     * Création d'une matrice vide
     * @return une matrice vide de 12x12
     */
    private static int[][] createEmptyMatrice() {
        int[][] matrice = new int[12][12];
        for (int[] ints : matrice) Arrays.fill(ints, 0);
        return matrice;
    }


    /**
     * Méthode d'affichage de la matrice de confusion
     * @param results les résultats des tests
     */
    public static void printMatrice(int[][] results) {
        int goodResultCount = 0;
        for (int numCol = 0; numCol <= 11; numCol++) {

            if (numCol == 10) {
                System.out.print("    +");
            } else if (numCol == 11) {
                System.out.print("    -\n");
            } else {
                System.out.print("    " + numCol);
            }
        }
        System.out.println("-------------------------------------------------------------");
        for (int numLine = 0; numLine <= 11; numLine++) {
            if (numLine == 10) {
                System.out.print("+" + " | ");
            } else if (numLine == 11) {
                System.out.print("-" + " | ");
            } else {
                System.out.print(numLine + " | ");
            }
            for (int info = 0; info <= 11; info++) {
                int result = results[numLine][info];
                if(numLine == info) goodResultCount += result;
                System.out.print(result + (result > 9 ? "   " : "    "));
            }
            System.out.println(" ");
        }
        System.out.println("-------------------------------------------------------------");
        System.out.println("Le taux de reconnaissance est de " + (goodResultCount * 120) / 100 + " %");
        System.exit(0);
    }

    /**
     * Récupère le numéro correspondant à l'image
     * @param image L'image concernée
     * @return le numéro correspondant à l'image
     */
    private static int getIntegerImage(ImagePlus image) {
        char c = image.getTitle().toCharArray()[0];
        if (c == '+') return 10;
        if (c == '-') return 11;
        return Character.getNumericValue(c);
    }

}