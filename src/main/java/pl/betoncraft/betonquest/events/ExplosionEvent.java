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
package pl.betoncraft.betonquest.events;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import pl.betoncraft.betonquest.InstructionParseException;
import pl.betoncraft.betonquest.api.QuestEvent;

/**
 * Spawns an explosion in a given location and with given flags
 * 
 * @author Dzejkop
 */
public class ExplosionEvent extends QuestEvent {

    private final boolean  setsFire;
    private final boolean  breaksBlocks;
    private final float    power;
    private final Location loc;

    public ExplosionEvent(String packName, String instructions)
            throws InstructionParseException {
        super(packName, instructions);
        staticness = true;
        String[] parts = instructions.split(" ");
        if (parts.length < 5) {
            throw new InstructionParseException("Not enough arguments");
        }
        setsFire = parts[1].equals("1") ? true : false;
        breaksBlocks = parts[2].equals("1") ? true : false;
        try {
            power = Float.parseFloat(parts[3]);
        } catch (NumberFormatException e) {
            throw new InstructionParseException("Could not parse power");
        }
        String[] partsOfLoc = parts[4].split(";");
        if (partsOfLoc.length != 4) {
            throw new InstructionParseException("Wrong location format");
        }
        World world = Bukkit.getWorld(partsOfLoc[3]);
        if (world == null) {
            throw new InstructionParseException("World " + partsOfLoc[3]
                    + " does not exists.");
        }
        double x, y, z;
        try {
            x = Double.parseDouble(partsOfLoc[0]);
            y = Double.parseDouble(partsOfLoc[1]);
            z = Double.parseDouble(partsOfLoc[2]);
        } catch (NumberFormatException e) {
            throw new InstructionParseException("Could not parse coordinates");
        }
        loc = new Location(world, x, y, z);

    }

    @Override
    public void run(String playerID) {
        loc.getWorld().createExplosion(loc.getX(), loc.getY(), loc.getZ(),
                power, setsFire, breaksBlocks);
    }
}
