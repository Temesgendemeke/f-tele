package com.example.ftele.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.ftele.R
import com.example.ftele.databinding.ItemRuleBinding
import com.example.ftele.model.Rule
import com.example.ftele.model.RuleAction

class RuleAdapter(
    private val onToggleRule: (Rule) -> Unit,
    private val onRemoveRule: (Rule) -> Unit
) : ListAdapter<Rule, RuleAdapter.RuleViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RuleViewHolder {
        val binding = ItemRuleBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return RuleViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RuleViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class RuleViewHolder(private val b: ItemRuleBinding) :
        RecyclerView.ViewHolder(b.root) {

        fun bind(rule: Rule) {
            val ctx = b.root.context
            b.tvRuleValue.text = rule.value

            b.cardRule.alpha = if (rule.enabled) 1.0f else 0.55f

            b.switchRule.setOnCheckedChangeListener(null)
            b.switchRule.isChecked = rule.enabled
            b.switchRule.setOnCheckedChangeListener { _, _ ->
                onToggleRule(rule)
            }

            val senderPart = if (!rule.senderFilter.isNullOrBlank()) "From [${rule.senderFilter}] · " else ""
            val catPart = if (!rule.category.isNullOrBlank()) " · ${rule.category}" else ""
            b.tvRuleMeta.text = "$senderPart${rule.target.label} · ${rule.mode.label}$catPart"

            if (rule.action == RuleAction.ALLOW) {
                b.tvRuleChip.text = "Allow"
                b.tvRuleChip.setTextColor(ContextCompat.getColor(ctx, R.color.allow))
                b.tvRuleChip.setBackgroundResource(R.drawable.bg_chip_shown)
            } else {
                b.tvRuleChip.text = "Block"
                b.tvRuleChip.setTextColor(ContextCompat.getColor(ctx, R.color.block))
                b.tvRuleChip.setBackgroundResource(R.drawable.bg_chip_muted)
            }

            b.btnRemoveRule.setOnClickListener {
                onRemoveRule(rule)
            }
        }
    }

    object DiffCallback : DiffUtil.ItemCallback<Rule>() {
        override fun areItemsTheSame(oldItem: Rule, newItem: Rule): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Rule, newItem: Rule): Boolean =
            oldItem == newItem
    }
}
