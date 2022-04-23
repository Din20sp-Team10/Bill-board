package com.example.billboard


import androidx.compose.material.ScaffoldState
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.CoroutineScope
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun GroupViewNavigationContainer(
    navControl: NavController,
    groupInfo: GroupClass,
    scState: ScaffoldState,
    groupsVM: GroupsViewModel,
    scope: CoroutineScope,
    userVM : UserViewModel
) {

    val expensesVM: ExpensesViewModel = viewModel()

    expensesVM.getExpenses( groupInfo.id )

    val expenses = expensesVM.expenses

    val expenseNavControl = rememberNavController()

    NavHost(navController = expenseNavControl, startDestination = "group" ) {
        composable( route = "group" ) {
            GroupView( groupInfo, expenses, expenseNavControl, navControl, scState, scope, groupsVM, userVM)
        }
        composable( route = "addMembers") {
            AddEditMemberView(
                groupsVM,
                userVM,
                scState,
                scope,
                groupInfo,
                expenseNavControl,
                navControl
            )
        }
        composable( route = "groupBalance" ) {
            GroupBalanceView(scState, expenseNavControl, groupInfo, expenses , scope, navControl, userVM, groupsVM)
        }
        composable( route = "addExpense") {
            val name = ""
            val amount = 0.0
            val payer = ""
            val expid = ""
            val date = SimpleDateFormat("yyyy/MM/dd_HH/mm/ss").format(Date()).toString()
            val rest = mutableListOf<String>()
            val paidvalues = mutableMapOf<String,Boolean>()
            val receiptURL = ""
            val expense = ExpenseClass( name, amount, payer, date, groupInfo.id, rest, expid, paidvalues, receiptURL)
            AddEditExpenseView(groupInfo, expenseNavControl, expensesVM, expense, scState, groupsVM, scope, userVM)

        }
        expenses.forEach { expense ->
            composable( route = expense.expid) {
                ExpenseView( expense, expenseNavControl, scState, scope, expensesVM, groupsVM, groupInfo, navControl, userVM)
            }
            composable( route = "${expense.expid}_addReceipt") {
                AddReceipt( expense, expenseNavControl, scState, scope, expensesVM )
            }
            composable( route = "${expense.expid}_showReceipt"){
                ReceiptView(
                    expenseID = expense.expid,
                    receiptURL = expense.receiptURL,
                    expenseNavControl,
                    scState,
                    scope
                )
            }
            composable( route = "${expense.expid}_edit"){
                AddEditExpenseView(groupInfo, expenseNavControl, expensesVM, expense, scState, groupsVM, scope, userVM)
            }
        }
    }
}

