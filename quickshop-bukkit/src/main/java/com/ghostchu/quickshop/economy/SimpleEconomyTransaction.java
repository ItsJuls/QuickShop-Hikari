/*
 *  This file is a part of project QuickShop, the name is EconomyTransaction.java
 *  Copyright (C) Ghost_chu and contributors
 *
 *  This program is free software: you can redistribute it and/or modify it
 *  under the terms of the GNU General Public License as published by the
 *  Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT
 *  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 *  FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *  for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.ghostchu.quickshop.economy;

import com.ghostchu.quickshop.QuickShop;
import com.ghostchu.quickshop.api.economy.EconomyCore;
import com.ghostchu.quickshop.api.economy.EconomyTransaction;
import com.ghostchu.quickshop.api.economy.operation.DepositEconomyOperation;
import com.ghostchu.quickshop.api.economy.operation.WithdrawEconomyOperation;
import com.ghostchu.quickshop.api.operation.Operation;
import com.ghostchu.quickshop.util.CalculateUtil;
import com.ghostchu.quickshop.util.JsonUtil;
import com.ghostchu.quickshop.util.MsgUtil;
import com.ghostchu.quickshop.util.Util;
import com.ghostchu.quickshop.util.logger.Log;
import com.ghostchu.quickshop.util.logging.container.EconomyTransactionLog;
import lombok.Builder;
import lombok.ToString;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.UUID;
import java.util.logging.Level;

/**
 * Transaction
 * A secure way to transfer the money between players.
 * Support rollback :)
 */
@ToString
public class SimpleEconomyTransaction implements EconomyTransaction {
    @Nullable
    private UUID from;
    @Nullable
    private UUID to;
    private double amount;
    @NotNull
    @JsonUtil.Hidden
    private EconomyCore core;
    private double amountAfterTax;
    private double tax;
    @Nullable
    private UUID taxer;
    private boolean allowLoan;
    private boolean tryingFixBalanceInsufficient;
    private World world;
    @Nullable
    private String currency;
    @JsonUtil.Hidden
    private final QuickShop plugin = QuickShop.getInstance();
    @Nullable
    private String lastError = null;
    private final Stack<Operation> processingStack = new Stack<>();


    /**
     * Create a transaction
     *
     * @param from        The account that money from, but null will be ignored.
     * @param to          The account that money to, but null will be ignored.
     * @param core        economy core
     * @param allowLoan   allow loan?
     * @param amount      the amount of money
     * @param taxAccount  tax account
     * @param taxModifier tax modifier
     */

    @Builder
    public SimpleEconomyTransaction(@Nullable UUID from, @Nullable UUID to, double amount, double taxModifier, @Nullable UUID taxAccount, EconomyCore core, boolean allowLoan, @NotNull World world, @Nullable String currency) {
        this.from = from;
        this.to = to;
        this.core = core == null ? QuickShop.getInstance().getEconomy() : core;
        this.amount = amount;
        this.taxer = taxAccount;
        this.allowLoan = allowLoan;
        this.world = world;
        this.currency = currency;

        if (Double.doubleToLongBits(taxModifier) != Double.doubleToLongBits(0.0d)) { //Calc total money and apply tax
            this.amountAfterTax = CalculateUtil.multiply(CalculateUtil.subtract(1, taxModifier), amount);
        } else {
            this.amountAfterTax = amount;
        }
        this.tax = CalculateUtil.subtract(amount, amountAfterTax); //Calc total tax
        if (from == null && to == null) {
            lastError = "From and To cannot be null in same time.";
            throw new IllegalArgumentException("From and To cannot be null in same time.");
        }
        this.tryingFixBalanceInsufficient = QuickShop.getInstance().getConfig().getBoolean("trying-fix-banlance-insuffient");
        if (tryingFixBalanceInsufficient) {
            //Fetch some stupid plugin caching
            if (from != null) {
                this.core.getBalance(from, world, currency);
            }
            if (to != null) {
                this.core.getBalance(to, world, currency);
            }
        }
    }
    @Override
    public @Nullable UUID getFrom() {
        return from;
    }
    @Override
    public void setFrom(@Nullable UUID from) {
        this.from = from;
    }
    @Override
    public @Nullable UUID getTo() {
        return to;
    }
    @Override
    public void setTo(@Nullable UUID to) {
        this.to = to;
    }
    @Override
    public double getAmount() {
        return amount;
    }
    @Override
    public void setAmount(double amount) {
        this.amount = amount;
    }
    @Override
    public double getAmountAfterTax() {
        return amountAfterTax;
    }
    @Override
    public void setAmountAfterTax(double amountAfterTax) {
        this.amountAfterTax = amountAfterTax;
    }
    @Override
    public @NotNull EconomyCore getCore() {
        return core;
    }
    @Override
    public void setCore(@NotNull EconomyCore core) {
        this.core = core;
    }
    @Override
    public @NotNull Stack<Operation> getProcessingStack() {
        return processingStack;
    }

    @Override
    public @Nullable String getCurrency() {
        return currency;
    }
    @Override
    public void setCurrency(@Nullable String currency) {
        this.currency = currency;
    }
    @Override
    public @Nullable UUID getTaxer() {
        return taxer;
    }
    @Override
    public void setTaxer(@Nullable UUID taxer) {
        this.taxer = taxer;
    }
    @Override
    @NotNull
    public World getWorld() {
        return world;
    }
    @Override
    public void setWorld(@NotNull World world) {
        this.world = world;
    }
    @Nullable
    @Override
    public String getLastError() {
        return lastError;
    }
    @Override
    public void setLastError(@NotNull String lastError) {
        this.lastError = lastError;
    }
    @Override
    public void setTax(double tax) {
        this.tax = tax;
    }
    @Override
    public void setAllowLoan(boolean allowLoan) {
        this.allowLoan = allowLoan;
    }
    @Override
    public void setTryingFixBalanceInsufficient(boolean tryingFixBalanceInsufficient) {
        this.tryingFixBalanceInsufficient = tryingFixBalanceInsufficient;
    }

    /**
     * Commit the transaction by the Fail-Safe way
     * Automatic rollback when commit failed
     *
     * @return The transaction success.
     */
    @Override
    public boolean failSafeCommit() {
        Log.transaction("Transaction begin: FailSafe Commit --> " + from + " => " + to + "; Amount: " + amount + ", EconomyCore: " + core.getName());
        boolean result = commit();
        if (!result) {
            Log.transaction(Level.WARNING, "Fail-safe commit failed, starting rollback: " + lastError);
            rollback(true);
        }
        return result;
    }

    /**
     * Commit the transaction
     *
     * @return The transaction success.
     */
    @Override
    public boolean commit() {
        return this.commit(new SimpleTransactionCallback() {
            @Override
            public void onSuccess(@NotNull SimpleEconomyTransaction economyTransaction) {
                if (tryingFixBalanceInsufficient) {
                    //Fetch some stupid plugin caching
                    if (from != null) {
                        core.getBalance(from, world, currency);
                    }
                    if (to != null) {
                        core.getBalance(to, world, currency);
                    }
                }
            }
        });
    }


    /**
     * Commit the transaction with callback
     *
     * @param callback The result callback
     * @return The transaction success.
     */
    @Override
    public boolean commit(@NotNull TransactionCallback callback) {
        Log.transaction("Transaction begin: Regular Commit --> " + from + " => " + to + "; Amount: " + amount + " Total(after tax): " + amountAfterTax + " Tax: " + tax + ", EconomyCore: " + core.getName());
        if (!callback.onCommit(this)) {
            this.lastError = "Plugin cancelled this transaction.";
            return false;
        }
        if (!checkBalance()) {
            this.lastError = "From hadn't enough money";
            callback.onFailed(this);
            return false;
        }


        if (from != null && !this.executeOperation(new WithdrawEconomyOperation(from, amount, world, currency, core))) {
            this.lastError = "Failed to withdraw " + amount + " from player " + from + " account. LastError: " + core.getLastError();
            callback.onFailed(this);
            return false;
        }
        if (to != null && !this.executeOperation(new DepositEconomyOperation(to, amountAfterTax, world, currency, core))) {
            this.lastError = "Failed to deposit " + amountAfterTax + " to player " + to + " account. LastError: " + core.getLastError();
            callback.onFailed(this);
            return false;
        }
        if (tax > 0 && taxer != null && !this.executeOperation(new DepositEconomyOperation(taxer, tax, world, currency, core))) {
            this.lastError = "Failed to deposit tax account: " + tax + ". LastError: " + core.getLastError();
            callback.onTaxFailed(this);
            //Tax never should failed.
        }
        callback.onSuccess(this);
        return true;
    }

    /**
     * Checks this transaction can be finished
     *
     * @return The transaction can be finished (had enough money)
     */
    @Override
    public boolean checkBalance() {
        return from == null || !(core.getBalance(from, world, currency) < amount) || allowLoan;
    }

    /**
     * Getting the tax in this transaction
     *
     * @return The tax in this transaction
     */
    @Override
    public double getTax() {
        return tax;
    }

    private boolean executeOperation(@NotNull Operation operation) {
        if (operation.isCommitted()) {
            throw new IllegalStateException("Operation already committed");
        }
        if (operation.isRollback()) {
            throw new IllegalStateException("Operation already rolled back, you must create another new operation.");
        }
        try {
            boolean result = operation.commit();
            if (!result)
                return false;
            processingStack.push(operation);
            return true;
        } catch (Exception exception) {
            this.lastError = "Failed to execute operation: " + core.getLastError() + "; Operation: " + operation;
            return false;
        }
    }

    /**
     * Rolling back the transaction
     *
     * @param continueWhenFailed Continue when some parts of the rollback fails.
     * @return A list contains all steps executed. If "continueWhenFailed" is false, it only contains all success steps before hit the error. Else all.
     */
    @SuppressWarnings("UnusedReturnValue")
    @NotNull
    @Override
    public List<Operation> rollback(boolean continueWhenFailed) {
        List<Operation> operations = new ArrayList<>();
        while (!processingStack.isEmpty()) {
            Operation operation = processingStack.pop();
            if (!operation.isCommitted()) {
                continue;
            }
            if (operation.isRollback()) {
                continue;
            }
            try {
                boolean result = operation.rollback();
                if (!result) {
                    if (continueWhenFailed) {
                        operations.add(operation);
                        continue;
                    } else {
                        break;
                    }
                }
                operations.add(operation);
            } catch (Exception exception) {
                if (continueWhenFailed) {
                    operations.add(operation);
                    MsgUtil.debugStackTrace(exception.getStackTrace());
                } else {
                    plugin.getLogger().log(Level.WARNING, "Failed to rollback transaction: " + core.getLastError() + "; Operation: " + operation + "; Transaction: " + this);
                    break;
                }
            }
        }
        return operations;
    }


    public interface SimpleTransactionCallback extends TransactionCallback{
        /**
         * Calling while Transaction commit
         *
         * @param economyTransaction Transaction
         * @return Does commit event has been cancelled
         */
        default boolean onCommit(@NotNull SimpleEconomyTransaction economyTransaction) {
            return true;
        }

        /**
         * Calling while Transaction commit successfully
         *
         * @param economyTransaction Transaction
         */
        default void onSuccess(@NotNull SimpleEconomyTransaction economyTransaction) {
            Log.transaction("Transaction succeed: " + economyTransaction);
            QuickShop.getInstance().logEvent(new EconomyTransactionLog(true, economyTransaction.getFrom(), economyTransaction.getTo(), economyTransaction.getCurrency(), economyTransaction.getTax(), economyTransaction.getTaxer() == null ? Util.getNilUniqueId() : economyTransaction.getTaxer(), economyTransaction.getAmount(), economyTransaction.getLastError()));
        }

        /**
         * Calling while Transaction commit failed
         * Use EconomyTransaction#getLastError() to getting reason
         * Use EconomyTransaction#getSteps() to getting the fail step
         *
         * @param economyTransaction Transaction
         */
        default void onFailed(@NotNull SimpleEconomyTransaction economyTransaction) {
            Log.transaction(Level.WARNING, "Transaction failed: " + economyTransaction.getLastError() + ", transaction: " + economyTransaction);
            QuickShop.getInstance().logEvent(new EconomyTransactionLog(false, economyTransaction.getFrom(), economyTransaction.getTo(), economyTransaction.getCurrency(), economyTransaction.getTax(), economyTransaction.getTaxer() == null ? Util.getNilUniqueId() : economyTransaction.getTaxer(), economyTransaction.getAmount(), economyTransaction.getLastError()));
        }

        /**
         * Calling while Tax processing failed
         * Use EconomyTransaction#getLastError() to getting reason
         * Use EconomyTransaction#getSteps() to getting the fail step
         *
         * @param economyTransaction Transaction
         */
        default void onTaxFailed(@NotNull SimpleEconomyTransaction economyTransaction) {
            Log.transaction(Level.WARNING, "Tax Transaction failed: " + economyTransaction.getLastError() + ", transaction: " + economyTransaction);
            QuickShop.getInstance().logEvent(new EconomyTransactionLog(false, economyTransaction.getFrom(), economyTransaction.getTo(), economyTransaction.getCurrency(), economyTransaction.getTax(), economyTransaction.getTaxer() == null ? Util.getNilUniqueId() : economyTransaction.getTaxer(), economyTransaction.getAmount(), economyTransaction.getLastError()));
        }

    }

}