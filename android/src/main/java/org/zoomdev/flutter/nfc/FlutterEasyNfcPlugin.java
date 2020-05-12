package org.zoomdev.flutter.nfc;

import android.app.Activity;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.TagLostException;
import android.nfc.tech.IsoDep;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.TagTechnology;

import androidx.annotation.NonNull;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;

import org.zoomdev.flutter.nfc.adapters.IsoDepTagAdapter;
import org.zoomdev.flutter.nfc.adapters.MifareOneTagAdapter;
import org.zoomdev.flutter.nfc.adapters.NfcTagAdapter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.embedding.engine.plugins.activity.ActivityAware;
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.embedding.engine.plugins.lifecycle.HiddenLifecycleReference;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry;
import io.flutter.plugin.common.PluginRegistry.Registrar;

/**
 * FlutterEasyNfcPlugin
 */
public class FlutterEasyNfcPlugin implements MethodCallHandler, NfcAdapterListener,
        PluginRegistry.NewIntentListener, FlutterPlugin, ActivityAware, LifecycleObserver {


    private NfcModel model;

    private Activity activity;

    private  MethodChannel channel;

    private Registrar registrar;

    private NfcTagAdapter tagAdapter;

    private ActivityPluginBinding activityPluginBinding;

    public static final String NOT_AVALIABLE = "1";
    private static final String NOT_ENABLED = "2";
    public static final String NOT_INITIALIZED = "3";
    public static final String IO = "4";
    public static final String IN_CORRECT_METHOD = "5";
    public static final String TAG_LOST = "6";
    public static final String COMMON_ERROR = "9";


    @Override
    public void onAttachedToEngine(@NonNull FlutterPluginBinding binding) {
        final MethodChannel channel = new MethodChannel(binding.getFlutterEngine().getDartExecutor(), "flutter_easy_nfc");
        channel.setMethodCallHandler(this);
        setDartChannel(channel);
    }

    @Override
    public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {

    }

    /**
     * Plugin registration.
     */
    public static void registerWith(Registrar registrar) {
        final MethodChannel channel = new MethodChannel(registrar.messenger(), "flutter_easy_nfc");
        FlutterEasyNfcPlugin plugin = new FlutterEasyNfcPlugin(registrar, channel);
        channel.setMethodCallHandler(plugin);

    }

    public FlutterEasyNfcPlugin() {}

    public FlutterEasyNfcPlugin(Registrar registrar, MethodChannel channel) {
        this.activity = registrar.activity();
        this.channel = channel;
        this.registrar = registrar;
    }

    public void setDartChannel(MethodChannel channel) {
        this.channel = channel;
    }


    @Override
    public void onAttachedToActivity(@NonNull ActivityPluginBinding binding) {
        activityPluginBinding = binding;
        activity = activityPluginBinding.getActivity();
        ((HiddenLifecycleReference) activityPluginBinding.getLifecycle()).getLifecycle().addObserver(this);
    }

    @Override
    public void onDetachedFromActivityForConfigChanges() {

    }

    @Override
    public void onReattachedToActivityForConfigChanges(@NonNull ActivityPluginBinding binding) {
        activityPluginBinding = binding;
    }

    @Override
    public void onDetachedFromActivity() {
        ((HiddenLifecycleReference) activityPluginBinding.getLifecycle()).getLifecycle().removeObserver(this);
    }

    void test(byte[] bytes) {
    }

    @Override
    public synchronized void onMethodCall(MethodCall call, Result result) {
        String method = call.method;


        if ("transceive".equals(method)) {
            transceive((byte[]) call.arguments, result);
        } else if ("authenticateSectorWithKeyA".equals(method)) {
            Map map = (Map) call.arguments;
            int sectorIndex = (int) map.get("sectorIndex");
            byte[] key = (byte[]) map.get("key");
            authenticateSectorWithKeyA(sectorIndex, key, result);
        } else if ("authenticateSectorWithKeyB".equals(method)) {
            Map map = (Map) call.arguments;
            int sectorIndex = (int) map.get("sectorIndex");
            byte[] key = (byte[]) map.get("key");
            authenticateSectorWithKeyB(sectorIndex, key, result);
        } else if ("readBlock".equals(method)) {
            Map map = (Map) call.arguments;
            int block = (int) map.get("block");
            readBlock(block, result);
        } else if ("writeBlock".equals(method)) {
            Map map = (Map) call.arguments;
            int block = (int) map.get("block");
            byte[] data = (byte[]) map.get("data");
            writeBlock(block, data, result);
        } else if ("transfer".equals(method)) {
            Map map = (Map) call.arguments;
            int block = (int) map.get("block");
            transfer(block, result);
        } else if ("restore".equals(method)) {
            Map map = (Map) call.arguments;
            int block = (int) map.get("block");
            restore(block, result);
        } else if ("increment".equals(method)) {
            Map map = (Map) call.arguments;
            int block = (int) map.get("block");
            int value = (int) map.get("value");
            increment(block, value, result);
        } else if ("decrement".equals(method)) {
            Map map = (Map) call.arguments;
            int block = (int) map.get("block");
            int value = (int) map.get("value");
            decrement(block, value, result);
        } else if ("getBlockCount".equals(method)) {
            getBlockCount(result);
        } else if ("getSectorCount".equals(method)) {
            getSectorCount(result);
        } else if ("connect".equals(method)) {
            connect(result);
        } else if ("close".equals(method)) {
            close(result);
        } else if ("isAvailable".equals(method)) {
            result.success(isAvailable());
        } else if ("isEnabled".equals(method)) {
            result.success(isEnabled());
        } else if ("startup".equals(method)) {
            startup(result);
        } else if ("shutdown".equals(method)) {
            shutdown();
            result.success(new HashMap<String, Object>());
        } else if ("resume".equals(method)) {
            resume();
            result.success(new HashMap<String, Object>());
        } else if ("pause".equals(method)) {
            pause();
            result.success(new HashMap<String, Object>());
        } else if ("sectorToBlock".equals(method)) {
            Map map = (Map) call.arguments;
            int sectorIndex = (int) map.get("sectorIndex");
            mifareClassicSectorToBlock(sectorIndex, result);
        } else if ("setTimeout".equals(method)) {
            Map map = (Map) call.arguments;
            int timeout = (int) map.get("timeout");
            setTimeout(timeout, result);
        } else if("mifareBlockSize".equals(method)) {
            result.success(mifareClassicBlockSize());
        }
        else {
            result.notImplemented();
        }
    }

    abstract class IsoDepNfcExector extends NfcExector<IsoDep> {

        @Override
        IsoDep get(NfcTagAdapter tagAdapter) {
            if (tagAdapter.getTag() instanceof IsoDep) {
                return (IsoDep) tagAdapter.getTag();
            }
            return null;
        }
    }

    abstract class MifareClassicNfcExecutor extends NfcExector<MifareClassic> {
        @Override
        MifareClassic get(NfcTagAdapter tagAdapter) {
            if (tagAdapter.getTag() instanceof MifareClassic) {
                return (MifareClassic) tagAdapter.getTag();
            }
            return null;
        }
    }

    abstract class BasicTagNfcExecutor extends NfcExector<TagTechnology> {
        @Override
        TagTechnology get(NfcTagAdapter tagAdapter) {
//            if (tagAdapter.getTag() instanceof MifareClassic) {
//                return (MifareClassic) tagAdapter.getTag();
//            }
//            else if(tagAdapter.getTag() instanceof IsoDep) {
//                return (IsoDep) tagAdapter.getTag();
//            }
            if (tagAdapter.getTag() instanceof TagTechnology) {
                return (TagTechnology) tagAdapter.getTag();
            }
            return null;
        }
    }


    abstract class NfcExector<T> {

        abstract T get(NfcTagAdapter tagAdapter);


        void handle(Result result) {
            if (tagAdapter == null) {
                processError(NOT_INITIALIZED, "Unknown NFC tag.", result);
                return;
            }

            T tag = get(tagAdapter);
            if (tag == null) {
                processError(IN_CORRECT_METHOD, "Invalid NFC tech.", result);
            } else {
                try {
                    Object data = execute(tag);
                    Map<String, Object> res = new HashMap<>();
                    res.put("data", data);
                    result.success(res);
                } catch (InvalidBlockException | InvalidSectorException e) {
                    processError(e.getErrorCode(), e.toString(), result);
                } catch (TagLostException e) {
                    processError(TAG_LOST, "NFC tag lost", result);
                } catch (Exception e) {
                    processError(COMMON_ERROR, e.getMessage(), result);
                }
            }
        }

        abstract Object execute(T tag) throws IOException, InvalidBlockException, InvalidSectorException;
    }

    private void transceive(final byte[] data, final Result result) {
        new IsoDepNfcExector() {
            @Override
            Object execute(IsoDep isoDep) throws IOException {
                return isoDep.transceive(data);
            }
        }.handle(result);
    }


    public boolean isAvailable() {
        return NfcAdapter.getDefaultAdapter(activity) != null;
    }

    public boolean isEnabled() {
        NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(activity);
        if (nfcAdapter == null) {
            return false;
        }
        return nfcAdapter.isEnabled();
    }

    public synchronized void startup(Result promise) {
        shutdown();
        NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(activity);
        if (nfcAdapter == null) {
            processError(NOT_AVALIABLE, "NFC is not available", promise);
            return;
        }
        if (!nfcAdapter.isEnabled()) {
            processError(NOT_ENABLED, "NFC is not enabled", promise);
            return;
        }
        model = new NfcModel(activity, new IsoDepTagAdapter.Factory(), new MifareOneTagAdapter.Factory());
        model.setAdapterListener(this);
        model.onResume(activity);
        model.onNewIntent(activity.getIntent());
        if(activityPluginBinding != null) {
            activityPluginBinding.addOnNewIntentListener(this);
        }
        if(registrar != null) {
            registrar.addNewIntentListener(this);
        }

        promise.success(new HashMap<String, Object>());
    }

    public synchronized void shutdown() {
        if (model != null) {
            model.onPause(activity);
            model.destroy();
            model = null;
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    public synchronized void resume() {
        if (model != null) {
            model.onResume(activity);
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    public synchronized void pause() {
        if (model != null) {
            model.onPause(activity);
        }
    }


    @Override
    public void onNfcAdapter(NfcTagAdapter tag) {

        tagAdapter = tag;
        Map<String, Object> data = new HashMap<>();
        data.put("tech", tag.getTech());

        if (tag.getTech().equals("MifareClassic")) {
            data.put("id", Utils.bytesToHex(((MifareClassic) tag.getTag()).getTag().getId()));
        } else if (tag.getTech().equals("IsoDep")) {
            data.put("id", Utils.bytesToHex(((IsoDep) tag.getTag()).getTag().getId()));
        }

        channel.invokeMethod("nfc", data);
    }

    public void close(Result promise) {
        if (tagAdapter != null) {
            tagAdapter.close();
            tagAdapter = null;
            processSuccess(promise);
        } else {
            processError(NOT_INITIALIZED, "Unable to close unidentified nfc tag.", promise);
        }
    }

    private void processSuccess(Result promise) {
        promise.success(new HashMap<>());
    }

    public void connect(Result promise) {
        synchronized (this) {
            if (tagAdapter != null) {
                try {
                    tagAdapter.connect();
                    processSuccess(promise);
                } catch (IOException e) {
                    processError(IO, "Unable to connect tag.", promise);
                }

            } else {
                processError(NOT_INITIALIZED, "Unable to connect unidentified tag.", promise);
            }
        }
    }

    @Override
    public boolean onNewIntent(Intent intent) {
        return model.onNewIntent(intent);
    }


    public void authenticateSectorWithKeyA(final int sectorIndex, final byte[] key, Result result) {
        synchronized (this) {
            new MifareClassicNfcExecutor() {
                @Override
                Object execute(MifareClassic tag) throws IOException, InvalidSectorException {
                    if (sectorIndex >= tag.getSectorCount()) {
                        throw new InvalidSectorException(String.format("invalid sector %d (max %d)", sectorIndex, tag.getSectorCount()));
                    }
                    return tag.authenticateSectorWithKeyA(sectorIndex, key);
                }
            }.handle(result);
        }
    }

    public void authenticateSectorWithKeyB(final int sectorIndex, final byte[] key, Result result) {
        synchronized (this) {
            new MifareClassicNfcExecutor() {
                @Override
                Object execute(MifareClassic tag) throws IOException, InvalidSectorException {
                    if (sectorIndex >= tag.getSectorCount()) {
                        throw new InvalidSectorException(String.format("invalid sector %d (max %d)", sectorIndex, tag.getSectorCount()));
                    }
                    return tag.authenticateSectorWithKeyB(sectorIndex, key);
                }
            }.handle(result);
        }
    }

    public void mifareClassicSectorToBlock(final int sectorIndex, Result result) {
        synchronized (this) {
            new MifareClassicNfcExecutor() {
                @Override
                Object execute(MifareClassic tag) throws IOException {
                    return tag.sectorToBlock(sectorIndex);
                }
            }.handle(result);
        }
    }

    public void setTimeout(final int timeout, Result result) {
        synchronized (this) {
            new BasicTagNfcExecutor() {
                @Override
                Object execute(TagTechnology tag) throws IOException, InvalidBlockException, InvalidSectorException {
                    if (tag instanceof MifareClassic) {
                        ((MifareClassic) tag).setTimeout(timeout);
                        return true;
                    } else if (tag instanceof IsoDep) {
                        ((IsoDep) tag).setTimeout(timeout);
                        return true;
                    }
                    return false;
                }
            }.handle(result);
        }
    }

    public synchronized int mifareClassicBlockSize() {
        return MifareClassic.BLOCK_SIZE;
    }


    public void readBlock(final int block, Result result) {
        synchronized (this) {
            new MifareClassicNfcExecutor() {
                @Override
                Object execute(MifareClassic tag) throws IOException, InvalidBlockException {
                    if (block >= tag.getBlockCount()) {
                        throw new InvalidBlockException(String.format("invalid block %d (max %d)", block, tag.getBlockCount()));
                    }
                    return tag.readBlock(block);
                }
            }.handle(result);
        }
    }

    public void getBlockCount(Result result) {
        synchronized (this) {
            new MifareClassicNfcExecutor() {
                @Override
                Object execute(MifareClassic tag) throws IOException {
                    return tag.getBlockCount();
                }
            }.handle(result);
        }
    }

    public void getSectorCount(Result result) {
        synchronized (this) {
            new MifareClassicNfcExecutor() {
                @Override
                Object execute(MifareClassic tag) throws IOException {
                    return tag.getSectorCount();
                }
            }.handle(result);
        }
    }

    public void writeBlock(final int block, final byte[] bytes, Result result) {
        synchronized (this) {
            new MifareClassicNfcExecutor() {
                @Override
                Object execute(MifareClassic tag) throws IOException {
                    tag.writeBlock(block, bytes);
                    return null;
                }
            }.handle(result);
        }
    }


    public void transfer(final int block, Result result) {
        synchronized (this) {
            new MifareClassicNfcExecutor() {
                @Override
                Object execute(MifareClassic tag) throws IOException {
                    tag.transfer(block);
                    return null;
                }
            }.handle(result);
        }
    }

    public void restore(final int block, Result result) {
        synchronized (this) {
            new MifareClassicNfcExecutor() {
                @Override
                Object execute(MifareClassic tag) throws IOException {
                    tag.restore(block);
                    return null;
                }
            }.handle(result);
        }
    }

    public void increment(final int block, final int value, Result result) {
        synchronized (this) {
            new MifareClassicNfcExecutor() {
                @Override
                Object execute(MifareClassic tag) throws IOException {
                    tag.increment(block, value);
                    return null;
                }
            }.handle(result);
        }
    }


    public void decrement(final int block, final int value, Result result) {
        synchronized (this) {
            new MifareClassicNfcExecutor() {
                @Override
                Object execute(MifareClassic tag) throws IOException {
                    tag.decrement(block, value);
                    return null;
                }
            }.handle(result);
        }
    }

    private void processError(String code, String message, Result promise) {
        Map<String, Object> data = new HashMap<>();
        data.put("code", code);
        data.put("message", message);

        promise.success(data);
    }


}
