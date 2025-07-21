package com.critetiontech.ctvitalio.UI.fragments


import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.critetiontech.ctvitalio.databinding.FragmentDevicesConnectivityBinding

class DevicesConnectivity : Fragment() {
    private var passedDeviceName: String? = null
    private var bluetoothAdapter: BluetoothAdapter? = null
    private var bluetoothLeScanner: BluetoothLeScanner? = null
    private var scanning = false
    private var targetDevice: BluetoothDevice? = null
    private var gatt: BluetoothGatt? = null
    private val SCAN_PERIOD: Long = 4000
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var binding: FragmentDevicesConnectivityBinding


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentDevicesConnectivityBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        passedDeviceName = arguments?.getString("deviceName")

        val bluetoothManager = requireContext().getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter

        binding.tvDeviceName.text = "Device Name: ${passedDeviceName ?: "Unknown"}"
        binding.tvConnectionStatus.text = "Status: Not Connected"

        binding.backIcon.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        binding.btnConnect.setOnClickListener {
            targetDevice?.let {
                connectToDevice(it)
            } ?: Toast.makeText(requireContext(), "No device selected yet", Toast.LENGTH_SHORT).show()
        }

        // 👉 Check and Request Permission
        checkAndRequestPermissions()
    }

    private fun checkAndRequestPermissions() {
        val permissionsNeeded = mutableListOf<String>()

        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                permissionsNeeded.add(Manifest.permission.BLUETOOTH_SCAN)
            }
        }
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                permissionsNeeded.add(Manifest.permission.BLUETOOTH_CONNECT)
            }
        }
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionsNeeded.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }

        if (permissionsNeeded.isNotEmpty()) {
            requestPermissions(permissionsNeeded.toTypedArray(), 1001)
        } else {
            startScanning() // ✅ If already granted, start scanning
        }
    }
    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == 1001) {
            if (grantResults.isNotEmpty()) {
                val grantedPermissions = permissions.zip(grantResults.toTypedArray()).toMap()

                val allPermissionsGranted = grantedPermissions.values.all { it == PackageManager.PERMISSION_GRANTED }

                if (allPermissionsGranted) {
                    startScanning()
                } else {
                    Toast.makeText(requireContext(), "All Bluetooth and Location permissions are required to scan devices", Toast.LENGTH_LONG).show()
                }
            } else {
                Toast.makeText(requireContext(), "Permission request canceled.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun startScanning() {
        if (bluetoothAdapter == null || !bluetoothAdapter!!.isEnabled) {
            Toast.makeText(requireContext(), "Please enable Bluetooth", Toast.LENGTH_SHORT).show()
            return
        }

        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(requireContext(), "Bluetooth scan permission not granted", Toast.LENGTH_SHORT).show()
            return
        }

        bluetoothLeScanner = bluetoothAdapter?.bluetoothLeScanner
        scanning = true
        bluetoothLeScanner?.startScan(scanCallback)
        updateStatus("Scanning...")

        handler.postDelayed({
            if (scanning) {
                scanning = false
                if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED) {
                    bluetoothLeScanner?.stopScan(scanCallback)
                }
                updateStatus("Scan Timeout")
            }
        }, SCAN_PERIOD)
    }
    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                return
            }

            val deviceName = result.device.name

            if (!deviceName.isNullOrEmpty() && deviceName == passedDeviceName) {
                targetDevice = result.device
                scanning = false
                bluetoothLeScanner?.stopScan(this)
                connectToDevice(targetDevice!!)
                updateStatus("Device Found: $deviceName")
            }
        }
        override fun onScanFailed(errorCode: Int) {
            scanning = false
            updateStatus("Scan Failed: $errorCode")
        }
    }

    private fun connectToDevice(device: BluetoothDevice) {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(requireContext(), "Bluetooth connect permission not granted", Toast.LENGTH_SHORT).show()
            return
        }

        gatt = device.connectGatt(requireContext(), false, gattCallback)
        updateStatus("Connecting...")
    }

    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            super.onConnectionStateChange(gatt, status, newState)

            if (newState == BluetoothProfile.STATE_CONNECTED) {
                updateStatus("Connected")
                if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
                    gatt.discoverServices()
                }
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                updateStatus("Disconnected")
                gatt.close()
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                updateStatus("Services Discovered")
            }
        }
    }

    private fun updateStatus(message: String) {
        activity?.runOnUiThread {
            binding.tvConnectionStatus.text = "Status: $message"
        }
    }

}