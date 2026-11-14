# Present (christmas) created via BDEngine

execute as @e[type=minecraft:block_display,tag=presentchristmas] on passengers unless entity @s[type=player] run kill @s
execute as @e[type=minecraft:block_display,tag=presentchristmas] run kill @s
