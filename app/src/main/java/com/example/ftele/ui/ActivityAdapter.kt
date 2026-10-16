package com.example.ftele.ui

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.ftele.R
import com.example.ftele.databinding.ItemLogBinding
import com.example.ftele.engine.Patterns
import com.example.ftele.model.LogEntry
import com.example.ftele.model.Rule
import com.example.ftele.model.RuleAction
import com.example.ftele.model.RuleTarget

class ActivityAdapter(
    private val onAllowSender: (String) -> Unit,
    private val onBlockSender: (String) -> Unit
) : ListAdapter<LogEntry, ActivityAdapter.LogViewHolder>(DiffCallback) {

    private var currentRules: List<Rule> = emptyList()

    fun updateRules(rules: List<Rule>) {
        this.currentRules = rules
        notifyDataSetChanged()
    }

    private fun findSenderRule(sender: String): Rule? {
        val looseSender = Patterns.loose(sender)
        return currentRules.find {
            it.target == RuleTarget.SENDER && Patterns.loose(it.value) == looseSender
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LogViewHolder {
        val binding = ItemLogBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return LogViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LogViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class LogViewHolder(private val b: ItemLogBinding) :
        RecyclerView.ViewHolder(b.root) {

        fun bind(entry: LogEntry) {
            val ctx = b.root.context
            b.tvSender.text = if (entry.sender.isBlank()) "Unknown sender" else entry.sender
            b.tvTime.text = formatAgo(entry.time)
            b.tvBody.text = entry.text

            if (entry.muted) {
                b.tvChipStatus.text = "Muted"
                b.tvChipStatus.setTextColor(ContextCompat.getColor(ctx, R.color.block))
                b.tvChipStatus.setBackgroundResource(R.drawable.bg_chip_muted)
            } else {
                b.tvChipStatus.text = "Shown"
                b.tvChipStatus.setTextColor(ContextCompat.getColor(ctx, R.color.allow))
                b.tvChipStatus.setBackgroundResource(R.drawable.bg_chip_shown)
            }

            b.tvReason.text = entry.reason

            val rule = findSenderRule(entry.sender)

            // Allow button styling
            if (rule?.action == RuleAction.ALLOW) {
                b.btnAllowSender.text = "Allowed"
                b.btnAllowSender.setBackgroundColor(ContextCompat.getColor(ctx, R.color.allow))
                b.btnAllowSender.setTextColor(Color.WHITE)
                b.btnAllowSender.strokeWidth = 0
            } else {
                b.btnAllowSender.text = "Allow sender"
                b.btnAllowSender.setBackgroundColor(Color.TRANSPARENT)
                b.btnAllowSender.setTextColor(ContextCompat.getColor(ctx, R.color.ink))
                b.btnAllowSender.strokeColor = ColorStateList.valueOf(ContextCompat.getColor(ctx, R.color.line))
                b.btnAllowSender.strokeWidth = 2
            }

            // Block button styling
            if (rule?.action == RuleAction.BLOCK) {
                b.btnBlockSender.text = "Blocked"
                b.btnBlockSender.setBackgroundColor(ContextCompat.getColor(ctx, R.color.block))
                b.btnBlockSender.setTextColor(Color.WHITE)
                b.btnBlockSender.strokeWidth = 0
            } else {
                b.btnBlockSender.text = "Block sender"
                b.btnBlockSender.setBackgroundColor(Color.TRANSPARENT)
                b.btnBlockSender.setTextColor(ContextCompat.getColor(ctx, R.color.ink))
                b.btnBlockSender.strokeColor = ColorStateList.valueOf(ContextCompat.getColor(ctx, R.color.line))
                b.btnBlockSender.strokeWidth = 2
            }

            b.btnAllowSender.setOnClickListener { onAllowSender(entry.sender) }
            b.btnBlockSender.setOnClickListener { onBlockSender(entry.sender) }
        }
    }

    private fun formatAgo(timeMs: Long): String {
        val s = Math.max(1, (System.currentTimeMillis() - timeMs) / 1000)
        return when {
            s < 60 -> "${s}s ago"
            s < 3600 -> "${s / 60}m ago"
            s < 86400 -> "${s / 3600}h ago"
            else -> "${s / 86400}d ago"
        }
    }

    object DiffCallback : DiffUtil.ItemCallback<LogEntry>() {
        override fun areItemsTheSame(oldItem: LogEntry, newItem: LogEntry): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: LogEntry, newItem: LogEntry): Boolean =
            oldItem == newItem
    }
}
