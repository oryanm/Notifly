package net.notifly.core;

import android.content.Context;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.StringTokenizer;

import libsvm.svm;
import libsvm.svm_model;
import libsvm.svm_node;
import libsvm.svm_parameter;

public class SvmPredict
{
    private static SvmPredict instance = null;
    private Context context;
    private final String svm_type_table[] = { "c_svc","nu_svc","one_class","epsilon_svr","nu_svr" };
    private final String kernel_type_table[]={ "linear","polynomial","rbf","sigmoid","precomputed" };

    private SvmPredict(Context c)
    {
        context = c;
    }

    private double toDouble(String s)
    {
        return Double.valueOf(s).doubleValue();
    }

    private int predict(Object[] vector, svm_model model) throws IOException
    {
        svm_node[] x = new svm_node[vector.length];

        for(int i = 0; i < vector.length; i++)
        {
            x[i] = new svm_node();
            x[i].index = i + 1;
            x[i].value = toDouble(vector[i].toString());
        }

        return (int)svm.svm_predict(model,x);
    }

    private svm_model svmLoadModel() throws IOException
    {
        // read parameters
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader
                (context.openFileInput(SvmTrain.SVM_MODEL_FILE_NAME)));

        svm_model model = new svm_model();
        model.rho = null;
        model.probA = null;
        model.probB = null;
        model.label = null;
        model.nSV = null;

        if (readModelHeader(bufferedReader, model) == false)
        {
            System.err.print("ERROR: failed to read model\n");
            return null;
        }

        // read sv_coef and SV

        int m = model.nr_class - 1;
        int l = model.l;
        model.sv_coef = new double[m][l];
        model.SV = new svm_node[l][];

        for(int i=0;i<l;i++)
        {
            String line = bufferedReader.readLine();
            StringTokenizer st = new StringTokenizer(line," \t\n\r\f:");

            for(int k=0;k<m;k++)
                model.sv_coef[k][i] = toDouble(st.nextToken());
            int n = st.countTokens()/2;
            model.SV[i] = new svm_node[n];
            for(int j=0;j<n;j++)
            {
                model.SV[i][j] = new svm_node();
                model.SV[i][j].index = Integer.parseInt(st.nextToken());
                model.SV[i][j].value = toDouble(st.nextToken());
            }
        }

        bufferedReader.close();

        return model;
    }

    private boolean readModelHeader(BufferedReader bufferReader, svm_model model)
    {
        svm_parameter param = new svm_parameter();
        model.param = param;

        try
        {
            while(true)
            {
                String cmd = bufferReader.readLine();
                String arg = cmd.substring(cmd.indexOf(' ')+1);

                if(cmd.startsWith("svm_type"))
                {
                    int i;
                    for(i=0;i<svm_type_table.length;i++)
                    {
                        if(arg.indexOf(svm_type_table[i])!=-1)
                        {
                            param.svm_type=i;
                            break;
                        }
                    }
                    if(i == svm_type_table.length)
                    {
                        System.err.print("unknown svm type.\n");
                        return false;
                    }
                }
                else if(cmd.startsWith("kernel_type"))
                {
                    int i;
                    for(i=0;i<kernel_type_table.length;i++)
                    {
                        if(arg.indexOf(kernel_type_table[i])!=-1)
                        {
                            param.kernel_type=i;
                            break;
                        }
                    }
                    if(i == kernel_type_table.length)
                    {
                        System.err.print("unknown kernel function.\n");
                        return false;
                    }
                }
                else if(cmd.startsWith("degree"))
                    param.degree = Integer.parseInt(arg);
                else if(cmd.startsWith("gamma"))
                    param.gamma = toDouble(arg);
                else if(cmd.startsWith("coef0"))
                    param.coef0 = toDouble(arg);
                else if(cmd.startsWith("nr_class"))
                    model.nr_class = Integer.parseInt(arg);
                else if(cmd.startsWith("total_sv"))
                    model.l = Integer.parseInt(arg);
                else if(cmd.startsWith("rho"))
                {
                    int n = model.nr_class * (model.nr_class-1)/2;
                    model.rho = new double[n];
                    StringTokenizer st = new StringTokenizer(arg);
                    for(int i=0;i<n;i++)
                        model.rho[i] = toDouble(st.nextToken());
                }
                else if(cmd.startsWith("label"))
                {
                    int n = model.nr_class;
                    model.label = new int[n];
                    StringTokenizer st = new StringTokenizer(arg);
                    for(int i=0;i<n;i++)
                        model.label[i] = Integer.parseInt(st.nextToken());
                }
                else if(cmd.startsWith("probA"))
                {
                    int n = model.nr_class*(model.nr_class-1)/2;
                    model.probA = new double[n];
                    StringTokenizer st = new StringTokenizer(arg);
                    for(int i=0;i<n;i++)
                        model.probA[i] = toDouble(st.nextToken());
                }
                else if(cmd.startsWith("probB"))
                {
                    int n = model.nr_class*(model.nr_class-1)/2;
                    model.probB = new double[n];
                    StringTokenizer st = new StringTokenizer(arg);
                    for(int i=0;i<n;i++)
                        model.probB[i] = toDouble(st.nextToken());
                }
                else if(cmd.startsWith("nr_sv"))
                {
                    int n = model.nr_class;
                    model.nSV = new int[n];
                    StringTokenizer st = new StringTokenizer(arg);
                    for(int i=0;i<n;i++)
                        model.nSV[i] = Integer.parseInt(st.nextToken());
                }
                else if(cmd.startsWith("SV"))
                {
                    break;
                }
                else
                {
                    System.err.print("unknown text in model file: ["+cmd+"]\n");
                    return false;
                }
            }
        }
        catch(Exception e)
        {
            return false;
        }
        return true;
    }

    public static SvmPredict getInstance(Context c)
    {
        if(instance == null)
        {
            instance = new SvmPredict(c);
        }

        return instance;
    }

    public int Calc(Object[] vector) throws IOException
    {
        svm_model model = svmLoadModel();
        return predict(vector ,model);
    }
}
