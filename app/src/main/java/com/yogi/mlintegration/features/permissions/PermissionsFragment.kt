package com.yogi.mlintegration.features.permissions

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.viewModels
import com.yogi.mlintegration.R
import com.yogi.mlintegration.databinding.FragmentPermissionsBinding
import com.yogi.mlintegration.base.BaseFragment
import com.yogi.permissionslibrary.PermissionHandler

class PermissionsFragment : BaseFragment<FragmentPermissionsBinding>(
    R.layout.fragment_permissions
) {

    lateinit var permissionsHandler: PermissionHandler
    private val permissionsViewModel: PermissionsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        permissionsHandler = PermissionHandler(requireActivity())
        lifecycle.addObserver(permissionsHandler)
    }

    override fun bindView(view: View) = FragmentPermissionsBinding.bind(view)

    override fun initViews() {
        permissionsViewModel.text.observe(viewLifecycleOwner) {
            binding.textHome.text = it
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
                    permissionsViewModel.setMessage("Permission is Granted")
                } else {
                    permissionsViewModel.setMessage("Permission is Not Granted")
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
                permissionsViewModel.setMessage("Permission is Granted")
            }
        }
    }
}