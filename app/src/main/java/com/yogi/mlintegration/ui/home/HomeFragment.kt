package com.yogi.mlintegration.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.yogi.mlintegration.databinding.FragmentHomeBinding
import com.yogi.permissionslibrary.PermissionHandler

class HomeFragment : Fragment() {

    lateinit var permissionsHandler: PermissionHandler

    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        permissionsHandler = PermissionHandler(requireActivity())
        lifecycle.addObserver(permissionsHandler)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val homeViewModel = ViewModelProvider(this)[HomeViewModel::class.java]

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textHome
        homeViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }
        binding.btPermission.setOnClickListener {
            permissionsHandler.requestPermission(
                android.Manifest.permission.ACCESS_COARSE_LOCATION,
                showRationale = { onRationaleAccepted ->
                    AlertDialog.Builder(requireContext())
                        .setTitle("Rationale Dialog")
                        .setMessage("This Permission is Required to do something")
                        .setPositiveButton("Ok") { d, _ ->
                            d.dismiss()
                            onRationaleAccepted.invoke()
                        }
                        .setNegativeButton("Cancel") { d, _ ->
                            d.dismiss()
                        }
                        .show()
                },
                permanentlyDeclinedCallback = { openSettings ->
                    AlertDialog.Builder(requireContext())
                        .setTitle("Permission Permanently Declined")
                        .setMessage("This Permission is Required to do something, please provide this permission from App Settings")
                        .setPositiveButton("Open App Settings") { d, _ ->
                            d.dismiss()
                            openSettings.invoke()
                        }
                        .setNegativeButton("Cancel") { d, _ ->
                            d.dismiss()
                        }
                        .show()
                }
            ) { isGranted ->
                if (isGranted) {
                    homeViewModel.setMessage("Permission is Granted")
                } else {
                    homeViewModel.setMessage("Permission is Not Granted")
                }
            }
        }
        binding.btPermissionLooped.setOnClickListener {
            permissionsHandler.requestPermissionLoop(
                android.Manifest.permission.CAMERA,
                showRationale = { onRationaleAccepted ->
                    AlertDialog.Builder(requireContext())
                        .setTitle("Rationale Dialog")
                        .setMessage("This Permission is Required to do something")
                        .setPositiveButton("Ok") { d, _ ->
                            d.dismiss()
                            onRationaleAccepted.invoke()
                        }
                        .setCancelable(false)
                        .show()
                },
                permanentlyDeclinedCallback = { openSettings ->
                    AlertDialog.Builder(requireContext())
                        .setTitle("Permission Permanently Declined")
                        .setMessage("This Permission is Required to do something, please provide this permission from App Settings")
                        .setPositiveButton("Open App Settings") { d, _ ->
                            d.dismiss()
                            openSettings.invoke()
                        }
                        .setCancelable(false)
                        .show()
                }
            ) {
                homeViewModel.setMessage("Permission is Granted")
            }
        }
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}