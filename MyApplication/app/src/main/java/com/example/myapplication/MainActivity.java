package com.example.myapplication;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Build;
import android.os.Bundle;
import android.widget.TextView;

import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.DynamicArray;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.Utf8String;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthAccounts;
import org.web3j.protocol.core.methods.response.EthCall;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.EthGetTransactionCount;
import org.web3j.protocol.core.methods.response.EthGetTransactionReceipt;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.protocol.http.HttpService;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {

    private EthCall ethCall;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String[] account = null;

        TextView textView = (TextView)findViewById(R.id.testView) ;
        TextView textView2 = (TextView)findViewById(R.id.textView2) ;
        TextView textView3 = (TextView)findViewById(R.id.textView3) ;

        Web3j web3 = Web3j.build(new HttpService("http://211.232.75.147:9101"));

        try {
            EthAccounts ethAccounts = web3.ethAccounts().sendAsync().get();
            account = ethAccounts.getAccounts().toArray(new String[0]);
            textView.setText(account[0]);
            //textView2.setText(account[0]);
        } catch (ExecutionException e) {
            textView.setText("error1");
            e.printStackTrace();
        } catch (InterruptedException e) {
            textView.setText("error2");
            e.printStackTrace();
        }

        // Get Parameter
        List<Type> inputParams = new ArrayList<Type>();
        inputParams.add(new org.web3j.abi.datatypes.Utf8String("did"));
        System.out.println("---------------------------------------------");
        System.out.println(inputParams);

        // Set Parameter
        List<Type> inputParams2 = new ArrayList<Type>();
        inputParams2.add(new org.web3j.abi.datatypes.Utf8String("did"));
        inputParams2.add(new org.web3j.abi.datatypes.Utf8String("ttt"));
        System.out.println("---------------------------------------------");
        System.out.println(inputParams2);

        List<TypeReference<?>> returnTypes = Collections.<TypeReference<?>>emptyList();

        // Get Function
        Function function = new Function("getDidserialDoc",
                inputParams,
                Collections.singletonList(new TypeReference<DynamicArray<Utf8String>>() {}));

        // Set Function
        Function function2 = new Function("setDidSerialDoc",
                inputParams2,
                Collections.emptyList());

        String txData = FunctionEncoder.encode(function);

        /**
         *
         * account[0] Address => 0x87dA51B77b358f5749f1a977cf35bDc1F342b001
         * SmartContract Address => 0xE33557748593820bE5b9b63AA6Ab72817314FB8C
         *
         */
        // Set
        new Thread(() -> {
                try {
                    EthGetTransactionCount ethGetTransactionCount = web3.ethGetTransactionCount(
                            "0x87dA51B77b358f5749f1a977cf35bDc1F342b001", DefaultBlockParameterName.LATEST).sendAsync().get();
                    BigInteger nonce = ethGetTransactionCount.getTransactionCount();
                    Transaction transaction = Transaction.createFunctionCallTransaction("0x87dA51B77b358f5749f1a977cf35bDc1F342b001", nonce,
                            Transaction.DEFAULT_GAS,
                            BigInteger.valueOf(800000), "0xE33557748593820bE5b9b63AA6Ab72817314FB8C",
                            FunctionEncoder.encode(function2));
                    // 6. ethereum Call
                    EthSendTransaction ethSendTransaction = web3.ethSendTransaction(transaction).sendAsync().get();
                    Thread.sleep(5000);
                    String transactionHash = ethSendTransaction.getTransactionHash();

                    EthGetTransactionReceipt transactionReceipt = web3.ethGetTransactionReceipt(transactionHash).sendAsync().get();
                    System.out.println("---------------------------------------------");
                    System.out.println(nonce);
                    System.out.println(transaction);
                    System.out.println(transactionHash);
//                  System.out.println(transactionReceipt.getTransactionReceipt().isPresent());

                    if(transactionReceipt.getTransactionReceipt().isPresent() && (transactionReceipt.getResult().getStatus()).equals("0x1"))
                    {
                        System.out.println("transactionReceipt.getResult().getContractAddress() = " + transactionReceipt.getResult());
                        //textView3.setText(transactionReceipt.getResult().toString());
                        runOnUiThread(new Runnable () {
                            @Override
                            public void run() {
                                textView3.setText(transactionReceipt.getResult().toString());
                            }
                        });
                    }
                    else
                    {
                        System.out.println("transaction Error");
                    }

                    System.out.println("---------------------------------------------");
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
        }).start();

        // Get
        new Thread(() -> {
            try {
                ethCall = web3.ethCall(Transaction.createEthCallTransaction("0x87dA51B77b358f5749f1a977cf35bDc1F342b001", "0xE33557748593820bE5b9b63AA6Ab72817314FB8C", txData), DefaultBlockParameterName.LATEST).send();
                System.out.println("---------------------------------------------");
                System.out.println(ethCall.getResult());
                List<Type> decode = FunctionReturnDecoder.decode(ethCall.getResult(), function.getOutputParameters());
                System.out.println(decode.get(0).getValue().toString());
                System.out.println(decode);
                System.out.println(((DynamicArray)decode.get(0)).getValue().get(1));
                //textView2.setText(((DynamicArray)decode.get(0)).getValue().get(6).toString());
                runOnUiThread(new Runnable () {
                    @Override
                    public void run() {
                        textView2.setText(decode.get(0).getValue().toString());
                    }
                });
                System.out.println("getType = " + decode.get(0).getTypeAsString());
                System.out.println("---------------------------------------------");

            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();

    }
}