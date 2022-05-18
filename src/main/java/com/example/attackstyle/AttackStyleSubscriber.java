package com.example.attackstyle;

import static com.example.attackstyle.AttackStyleWithBonus.CASTING;
import static com.example.attackstyle.AttackStyleWithBonus.OTHER;

import java.util.concurrent.atomic.AtomicReference;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.Client;
import net.runelite.api.VarPlayer;
import net.runelite.api.Varbits;
import net.runelite.api.events.VarbitChanged;
import net.runelite.client.eventbus.Subscribe;

@Singleton
public final class AttackStyleSubscriber {

  private final AtomicReference<AttackStyleWithBonus> currentAttackStyleWithBonus =
      new AtomicReference<>(OTHER);
  private final Client client;

  @Inject
  AttackStyleSubscriber(Client client) {
    this.client = client;
  }

  public AttackStyle getCurrentAttackStyle() {
    return currentAttackStyleWithBonus.get().getAttackStyle();
  }

  public MeleeBonusType getCurrentMeleeBonusType() {
    return currentAttackStyleWithBonus.get().getMeleeBonusType();
  }

  @Subscribe
  public void onVarbitChanged(VarbitChanged event) {
    int attackStyleIndex = client.getVar(VarPlayer.ATTACK_STYLE);
    int equippedWeaponType = client.getVarbitValue(Varbits.EQUIPPED_WEAPON_TYPE);
    int defensiveCastingMode = client.getVarbitValue(Varbits.DEFENSIVE_CASTING_MODE);

    AttackStyleWithBonus[] attackStylesWithBonuses =
        WeaponType.getWeaponType(equippedWeaponType).getAttackStylesWithBonuses();
    if (attackStyleIndex < attackStylesWithBonuses.length) {
      AttackStyleWithBonus attackStyleWithBonus = attackStylesWithBonuses[attackStyleIndex];
      if (CASTING.equals(attackStyleWithBonus) && defensiveCastingMode == 1) {
        attackStyleWithBonus = AttackStyleWithBonus.DEFENSIVE_CASTING;
      }
      currentAttackStyleWithBonus.set(attackStyleWithBonus);
    }

    currentAttackStyleWithBonus.compareAndSet(null, OTHER);
  }
}
