/**
 * BetonQuest - advanced quests for Bukkit
 * Copyright (C) 2015  Jakub "Co0sh" Sapalski
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package pl.betoncraft.betonquest.conditions;

import java.util.ArrayList;

import pl.betoncraft.betonquest.BetonQuest;
import pl.betoncraft.betonquest.InstructionParseException;
import pl.betoncraft.betonquest.api.Condition;
import pl.betoncraft.betonquest.utils.Utils;

/**
 * Checks the conditions for the whole party (including the player that started
 * the checking)
 * 
 * @author Jakub Sapalski
 */
public class PartyCondition extends Condition {

    private final double   range;
    private final String[] conditions;
    private final String[] everyone;
    private final String[] anyone;
    private final int      count;

    public PartyCondition(String packName, String instructions)
            throws InstructionParseException {
        super(packName, instructions);
        String[] parts = instructions.split(" ");
        if (parts.length < 4) {
            throw new InstructionParseException("Not enough arguments");
        }
        // first argument is the distance
        try {
            range = Double.parseDouble(parts[1]);
            if (range <= 0) {
                throw new InstructionParseException("Range must be positive");
            }
        } catch (NumberFormatException e) {
            throw new InstructionParseException("Could not parse distance");
        }
        // next are conditions
        conditions = parts[2].split(",");
        for (int i = 0; i < conditions.length; i++) {
            if (!conditions[i].contains(".")) {
                conditions[i] = pack.getName() + "." + conditions[i];
            }
        }
        // now time for everything else
        String[] tempEvery = new String[] {}, tempAny = new String[] {};
        int tempCount = 0;
        for (String part : parts) {
            if (part.startsWith("every:")) {
                tempEvery = part.substring(6).split(",");
                for (int i = 0; i < tempEvery.length; i++) {
                    if (!tempEvery[i].contains(".")) {
                        tempEvery[i] = pack.getName() + "." + tempEvery[i];
                    }
                }
            } else if (part.startsWith("any:")) {
                tempAny = part.substring(4).split(",");
                for (int i = 0; i < tempAny.length; i++) {
                    if (!tempAny[i].contains(".")) {
                        tempAny[i] = pack.getName() + "." + tempAny[i];
                    }
                }
            } else if (part.startsWith("count:")) {
                try {
                    tempCount = Integer.parseInt(part.substring(6));
                } catch (NumberFormatException e) {
                    throw new InstructionParseException(
                            "Could not parse \"count\" argument");
                }
            }
        }
        everyone = tempEvery;
        anyone = tempAny;
        count = tempCount;
        // everything loaded
    }

    @Override
    public boolean check(String playerID) {
        // get the party
        ArrayList<String> members =
                Utils.getParty(playerID, range, pack.getName(), conditions);
        // check every condition against every player - all of them must meet
        // those conditions
        for (String condition : everyone) {
            for (String memberID : members) {
                // if this condition wasn't met by someone, return false
                if (!BetonQuest.condition(memberID, condition)) {
                    return false;
                }
            }
        }
        // check every condition against every player - at least one of them
        // must meet each of those
        for (String condition : anyone) {
            boolean met = false;
            for (String memberID : members) {
                if (BetonQuest.condition(memberID, condition)) {
                    met = true;
                    break;
                }
            }
            // if this condition wasn't met by anyone, return false
            if (!met) {
                return false;
            }
        }
        // if the count is more than 0, we need to check if there are more
        // players in the party than required minimum
        if (count > 0 && members.size() < count) {
            return false;
        }
        // every check was passed, the party meets all conditions
        return true;
    }

}
