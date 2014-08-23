package net.notifly.core;

import android.content.Context;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.StringTokenizer;
import java.util.Vector;

import libsvm.svm;
import libsvm.svm_model;
import libsvm.svm_node;
import libsvm.svm_parameter;
import libsvm.svm_problem;

public class SvmTrain
{
    private static SvmTrain instance;
    private final String svm_type_table[] = { "c_svc","nu_svc","one_class","epsilon_svr","nu_svr" };
    private final String kernel_type_table[]={ "linear","polynomial","rbf","sigmoid","precomputed" };
	private svm_parameter param;
	private svm_problem prob;
	private svm_model model;
	private String error_msg;
	private int cross_validation;
	private int nr_fold;
    private Context context;

    public static final String TRAINING_SET_FILE_NAME = "TrainingSet.txt";
    public static final String SVM_MODEL_FILE_NAME = "SVMModel.model";

    private SvmTrain(Context c)
    {
        context = c;

        param = new svm_parameter();
        // default values
        param.svm_type = svm_parameter.EPSILON_SVR;
        param.kernel_type = svm_parameter.RBF;
        param.degree = 3;
        param.gamma = 0.01;	// 1/num_features
        param.coef0 = 0;
        param.nu = 0.5;
        param.cache_size = 100;
        param.C = 15;
        param.eps = 1e-3;
        param.p = 0.1;
        param.shrinking = 1;
        param.probability = 0;
        param.nr_weight = 0;
        param.weight_label = new int[0];
        param.weight = new double[0];
        cross_validation = 0;

        File file = context.getFileStreamPath(TRAINING_SET_FILE_NAME);

        // Create trainingSet file with default vector if not exists.
        if (!file.exists() || file.length() == 0)
        {
            try
            {
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader
                        (context.getResources().getAssets().open(TRAINING_SET_FILE_NAME)));
                DataOutputStream outputStream = new DataOutputStream(new BufferedOutputStream
                        (context.openFileOutput(TRAINING_SET_FILE_NAME, Context.MODE_PRIVATE)));

                while(true) {
                    String line = bufferedReader.readLine();

                    if (line == null) break;

                    outputStream.writeBytes(line + "\n");
                }

                bufferedReader.close();
                outputStream.close();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }

	private void do_cross_validation()
	{
		int i;
		int total_correct = 0;
		double total_error = 0;
		double sumv = 0, sumy = 0, sumvv = 0, sumyy = 0, sumvy = 0;
		double[] target = new double[prob.l];

		svm.svm_cross_validation(prob,param,nr_fold,target);
		if(param.svm_type == svm_parameter.EPSILON_SVR ||
		   param.svm_type == svm_parameter.NU_SVR)
		{
			for(i=0;i<prob.l;i++)
			{
				double y = prob.y[i];
				double v = target[i];
				total_error += (v-y)*(v-y);
				sumv += v;
				sumy += y;
				sumvv += v*v;
				sumyy += y*y;
				sumvy += v*y;
			}
			System.out.print("Cross Validation Mean squared error = "+total_error/prob.l+"\n");
			System.out.print("Cross Validation Squared correlation coefficient = "+
				((prob.l*sumvy-sumv*sumy)*(prob.l*sumvy-sumv*sumy))/
				((prob.l*sumvv-sumv*sumv)*(prob.l*sumyy-sumy*sumy))+"\n"
				);
		}
		else
		{
			for(i=0;i<prob.l;i++)
				if(target[i] == prob.y[i])
					++total_correct;
			System.out.print("Cross Validation Accuracy = "+100.0*total_correct/prob.l+"%\n");
		}
	}

	private static double toDouble(String s)
	{
		double d = Double.valueOf(s).doubleValue();
		if (Double.isNaN(d) || Double.isInfinite(d))
		{
			System.err.print("NaN or Infinity in input\n");
			System.exit(1);
		}
		return(d);
	}

	// read in a problem (in svmlight format)
	private void read_problem() throws IOException
	{
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader
                (context.openFileInput(TRAINING_SET_FILE_NAME)));

		Vector<Double> vy = new Vector<Double>();
		Vector<svm_node[]> vx = new Vector<svm_node[]>();
		int max_index = 0;

		while(true)
		{
			String line = bufferedReader.readLine();
			if(line == null) break;

			StringTokenizer st = new StringTokenizer(line," \t\n\r\f:");

			vy.addElement(toDouble(st.nextToken()));
			int m = st.countTokens()/2;
			svm_node[] x = new svm_node[m];
			for(int j=0;j<m;j++)
			{
				x[j] = new svm_node();
				x[j].index = Integer.parseInt(st.nextToken());
				x[j].value = toDouble(st.nextToken());
			}
			if(m>0) max_index = Math.max(max_index, x[m-1].index);
			vx.addElement(x);
		}

		prob = new svm_problem();
		prob.l = vy.size();
		prob.x = new svm_node[prob.l][];
		for(int i=0;i<prob.l;i++)
			prob.x[i] = vx.elementAt(i);
		prob.y = new double[prob.l];
		for(int i=0;i<prob.l;i++)
			prob.y[i] = vy.elementAt(i);

		if(param.gamma == 0 && max_index > 0)
			param.gamma = 1.0/max_index;

		if(param.kernel_type == svm_parameter.PRECOMPUTED)
			for(int i=0;i<prob.l;i++)
			{
				if (prob.x[i][0].index != 0)
				{
					System.err.print("Wrong kernel matrix: first column must be 0:sample_serial_number\n");
					System.exit(1);
				}
				if ((int)prob.x[i][0].value <= 0 || (int)prob.x[i][0].value > max_index)
				{
					System.err.print("Wrong input format: sample_serial_number out of range\n");
					System.exit(1);
				}
			}

        bufferedReader.close();
	}

    private void svmSaveModel() throws IOException
    {
        DataOutputStream fp = new DataOutputStream(new BufferedOutputStream
                (context.openFileOutput(SVM_MODEL_FILE_NAME, Context.MODE_PRIVATE)));

        svm_parameter param = model.param;

        fp.writeBytes("svm_type " + svm_type_table[param.svm_type]+"\n");
        fp.writeBytes("kernel_type " + kernel_type_table[param.kernel_type]+"\n");

        if(param.kernel_type == svm_parameter.POLY) {
            fp.writeBytes("degree " + param.degree + "\n");
        }

        if(param.kernel_type == svm_parameter.POLY || param.kernel_type == svm_parameter.RBF ||
                param.kernel_type == svm_parameter.SIGMOID) {
            fp.writeBytes("gamma " + param.gamma + "\n");
        }

        if(param.kernel_type == svm_parameter.POLY || param.kernel_type == svm_parameter.SIGMOID) {
            fp.writeBytes("coef0 " + param.coef0 + "\n");
        }

        int nr_class = model.nr_class;
        int l = model.l;
        fp.writeBytes("nr_class "+nr_class+"\n");
        fp.writeBytes("total_sv "+l+"\n");
        {
            fp.writeBytes("rho");
            for(int i=0;i<nr_class*(nr_class-1)/2;i++) {
                fp.writeBytes(" " + model.rho[i]);
            }
            fp.writeBytes("\n");
        }

        if(model.label != null)
        {
            fp.writeBytes("label");
            for(int i=0;i<nr_class;i++) {
                fp.writeBytes(" " + model.label[i]);
            }
            fp.writeBytes("\n");
        }

        if(model.probA != null) // regression has probA only
        {
            fp.writeBytes("probA");
            for(int i=0;i<nr_class*(nr_class-1)/2;i++){
                fp.writeBytes(" "+model.probA[i]);
            }
            fp.writeBytes("\n");
        }

        if(model.probB != null)
        {
            fp.writeBytes("probB");
            for(int i=0;i<nr_class*(nr_class-1)/2;i++) {
                fp.writeBytes(" " + model.probB[i]);
            }
            fp.writeBytes("\n");
        }

        if(model.nSV != null)
        {
            fp.writeBytes("nr_sv");
            for(int i=0;i<nr_class;i++) {
                fp.writeBytes(" " + model.nSV[i]);
            }
            fp.writeBytes("\n");
        }

        fp.writeBytes("SV\n");
        double[][] sv_coef = model.sv_coef;
        svm_node[][] SV = model.SV;

        for(int i=0;i<l;i++)
        {
            for(int j=0;j<nr_class-1;j++) {
                fp.writeBytes(sv_coef[j][i] + " ");
            }

            svm_node[] p = SV[i];
            if(param.kernel_type == svm_parameter.PRECOMPUTED) {
                fp.writeBytes("0:" + (int) (p[0].value));
            }
            else {
                for (int j = 0; j < p.length; j++) {
                    fp.writeBytes(p[j].index + ":" + p[j].value + " ");
                }
            }

            fp.writeBytes("\n");
        }

        fp.close();
    }

    public static SvmTrain GetInstance(Context c)
    {
        if(instance == null)
        {
            instance = new SvmTrain(c);
        }

        return instance;
    }

    public void run() throws IOException
    {
        read_problem();
        error_msg = svm.svm_check_parameter(prob,param);

        if(error_msg != null)
        {
            System.err.print("ERROR: "+error_msg+"\n");
            System.exit(1);
        }

        if(cross_validation != 0)
        {
            do_cross_validation();
        }
        else
        {
            model = svm.svm_train(prob,param);
            svmSaveModel();
        }
    }
}
