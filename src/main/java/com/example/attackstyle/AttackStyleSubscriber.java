package com.example.attackstyle;

import static com.example.attackstyle.AttackStyleWithBonus.CASTING;
import static com.example.attackstyle.AttackStyleWithBonus.OTHER;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.Client;
import net.runelite.api.VarPlayer;
import net.runelite.api.Varbits;
import net.runelite.api.events.VarbitChanged;
import net.runelite.client.eventbus.Subscribe;

/**
 * A subscriber for changes to the player's currently selected attack style.
 *
 * <p>In addition to retaining the attack style, this class also finds and exposes the type of melee
 * bonus associated with the attack style (e.g. Slash, Crush, or Stab), if applicable.
 */
@Singleton
public final class AttackStyleSubscriber {

  private final AtomicReference<AttackStyleWithBonus> currentAttackStyleWithBonus =
      new AtomicReference<>(OTHER);
  private final Client client;

  @Inject
  AttackStyleSubscriber(Client client) {
    this.client = client;
  }

  /**
   * Returns the currently selected attack style. If there is none selected (i.e. if the player is
   * not logged in), {@link AttackStyle#OTHER} is returned.
   */
  public AttackStyle getCurrentAttackStyle() {
    return currentAttackStyleWithBonus.get().getAttackStyle();
  }

  /**
   * Returns the {@link MeleeBonusType} of the currently selected attack style. If either there is
   * no attack style selected (i.e. if the player is not logged in) or if the selected style is not
   * a melee type, then {@link MeleeBonusType#UNSPECIFIED} is returned.
   */
  public MeleeBonusType getCurrentMeleeBonusType() {
    return currentAttackStyleWithBonus.get().getMeleeBonusType();
  }

  @Subscribe
  private void onVarbitChanged(VarbitChanged event) {
    int attackStyleIndex = client.getVar(VarPlayer.ATTACK_STYLE);
    int equippedWeaponType = client.getVarbitValue(Varbits.EQUIPPED_WEAPON_TYPE);
    int defensiveCastingMode = client.getVarbitValue(Varbits.DEFENSIVE_CASTING_MODE);

    Optional<WeaponType> weaponType = WeaponType.getWeaponType(equippedWeaponType);
    if (!weaponType.isPresent()) {
      return;
    }

    AttackStyleWithBonus[] attackStylesWithBonuses = weaponType.get().getAttackStylesWithBonuses();
    if (attackStyleIndex < attackStylesWithBonuses.length) {
      AttackStyleWithBonus attackStyleWithBonus = attackStylesWithBonuses[attackStyleIndex];
      if (CASTING.equals(attackStyleWithBonus) && defensiveCastingMode == 1) {
        attackStyleWithBonus = AttackStyleWithBonus.DEFENSIVE_CASTING;
      }
      currentAttackStyleWithBonus.set(attackStyleWithBonus);
    }

    // Overwrites null referenced values to mean "OTHER" for convenience in the getter methods.
    currentAttackStyleWithBonus.compareAndSet(null, OTHER);
  }
}
