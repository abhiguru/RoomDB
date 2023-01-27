package `in`.tutorial.roomdb

import android.app.AlertDialog
import android.app.Dialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import `in`.tutorial.roomdb.databinding.ActivityMainBinding
import `in`.tutorial.roomdb.databinding.DialogUpdateBinding
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private var binding:ActivityMainBinding? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding?.root)
        val employeeDao = (application as EmployeeApp).db.employeeDao()
        binding?.btnAdd?.setOnClickListener {
            addRecord(employeeDao)
        }
        lifecycleScope.launch {
            employeeDao.fetchAllEmployee().collect(){
                val arrayList = ArrayList(it)
                setupListOfDataIntoRecyclerView(arrayList, employeeDao)
            }
        }
    }
    fun addRecord(employeeDao:EmployeeDao){
        val name = binding?.etName?.text.toString()
        val email = binding?.etEmailId?.text.toString()
        if(email.isNotEmpty() && name.isNotEmpty()){
            lifecycleScope.launch {
                val employeeEntity = EmployeeEntity(name=name, email=email)
                employeeDao.insert(employeeEntity)
                Toast.makeText(this@MainActivity, "Record Saved", Toast.LENGTH_LONG).show()
                binding?.etName?.text?.clear()
                binding?.etEmailId?.text?.clear()
            }
        }else{
            Toast.makeText(this@MainActivity, "Blank record cannot be inserted",
                Toast.LENGTH_LONG).show()
        }
    }
    private fun setupListOfDataIntoRecyclerView(
        employeeList:ArrayList<EmployeeEntity>,
        employeeDao: EmployeeDao){
        if(employeeList.isNotEmpty()){
            val itemAdapter = ItemAdapter(employeeList, {
                updateId->updateRecordDialog(updateId, employeeDao)
            }, {
                deleteId->deleteRecordAlertDialog(deleteId, employeeDao)
            })
            binding?.rvItemsList?.layoutManager= LinearLayoutManager(this)
            binding?.rvItemsList?.adapter = itemAdapter
            binding?.rvItemsList?.visibility = View.VISIBLE
            binding?.tvNoRecordsAvailable?.visibility = View.GONE
        }else{
            binding?.rvItemsList?.visibility = View.GONE
            binding?.tvNoRecordsAvailable?.visibility = View.VISIBLE
        }
    }

    private fun updateRecordDialog(id:Int, employeeDao: EmployeeDao){
        val updateDialog = Dialog(this, R.style.Theme_Dialog)
        updateDialog.setCancelable(false)
        val binding = DialogUpdateBinding.inflate(layoutInflater)
        updateDialog.setContentView(binding.root)

        lifecycleScope.launch{
            employeeDao.fetchEmployeeById(id).collect{
                if(it!=null) {
                    binding.etUpdateName.setText(it.name)
                    binding.etUpdateEmailId.setText(it.email)
                }
            }
        }
        binding.tvUpdate.setOnClickListener {
            val name = binding.etUpdateName.text.toString()
            val email = binding.etUpdateEmailId.text.toString()
            if(name.isNotEmpty() && email.isNotEmpty()){
                lifecycleScope.launch{
                    employeeDao.update(EmployeeEntity(id, name, email))
                    Toast.makeText(this@MainActivity, "Record updated",
                        Toast.LENGTH_LONG).show()
                    updateDialog.dismiss()
                }
            }else{
                Toast.makeText(this@MainActivity, "Blank Record",
                    Toast.LENGTH_LONG).show()
            }
        }
        binding.tvCancel.setOnClickListener {
            updateDialog.dismiss()
        }
        updateDialog.show()
    }

    private fun deleteRecordAlertDialog(id:Int, employeeDao: EmployeeDao){
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Are you sure?")
        builder.setIcon(R.drawable.ic_dialog_alert)
        builder.setPositiveButton("Yes"){
            dialogInterface, _ ->
            lifecycleScope.launch{
                employeeDao.delete(EmployeeEntity(id))
                Toast.makeText(this@MainActivity, "Deleted Record",
                    Toast.LENGTH_LONG).show()
            }
            dialogInterface.dismiss()
        }
        builder.setNegativeButton("No"){
            dialogInterface, _ ->
            dialogInterface.dismiss()
        }
        val alertDialog:AlertDialog = builder.create()
        alertDialog.setCancelable(false)
        alertDialog.show()
    }
}