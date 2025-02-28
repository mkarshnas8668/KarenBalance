import android.annotation.SuppressLint
import android.app.Activity
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mkarshnas6.karenstudio.karenbalance.ExpenseEntity
import com.mkarshnas6.karenstudio.karenbalance.OnExpenseClickListener
import com.mkarshnas6.karenstudio.karenbalance.databinding.ListItemExpensesBinding
import com.mkarshnas6.karenstudio.karenbalance.format_number


class ExpensesRecyclerAdapter(
    private val context: Activity,
    private val listener: OnExpenseClickListener
) : RecyclerView.Adapter<ExpensesRecyclerAdapter.ExpenseViewHolder>() {

    private val expensesList = mutableListOf<ExpenseEntity>()
    private val PersianDateToday = PersianDate.getCurrentShamsiDate()

    private var last_date_message = "Null"

    inner class ExpenseViewHolder(
        private val binding: ListItemExpensesBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("SetTextI18n")
        fun bind(expense: ExpenseEntity) {
            last_date_message = expense.date
            binding.txtPrice.text = expense.price.toString().format_number()
            binding.txtDescription.text = expense.description
            if (expense.date == PersianDateToday)
                binding.txtDateTime.text = "Today \n ${expense.time}"
            else
                binding.txtDateTime.text = "${expense.date} \n ${expense.time}"

//            set for description and btn edit if click on this
            binding.btnEditReportExpense.setOnClickListener {
                listener.onExpenseClick(expense)
            }

            binding.txtDescription.setOnClickListener {
                listener.onExpenseClick(expense)
            }
//            end click on description and btn edit


        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpenseViewHolder {
        val binding = ListItemExpensesBinding.inflate(
            LayoutInflater.from(context),
            parent,
            false
        )
        return ExpenseViewHolder(binding)
    }

    override fun getItemCount(): Int = expensesList.size

    override fun onBindViewHolder(holder: ExpenseViewHolder, position: Int) {
        holder.bind(expensesList[position])
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setExpenses(expenses: List<ExpenseEntity>) {
        expensesList.clear()
        expensesList.addAll(expenses)
        notifyDataSetChanged()
    }
}
