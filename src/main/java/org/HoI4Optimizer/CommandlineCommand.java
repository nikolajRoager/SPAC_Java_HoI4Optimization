package org.HoI4Optimizer;

import com.diogonunes.jcolor.Attribute;

import java.util.List;

import static com.diogonunes.jcolor.Ansi.colorize;

public class CommandlineCommand
{
    /// What is this command?
    private final String command;
    /// What does this command do in one line?
    private final String description;

    public static class Argument
    {
        public String command;
        public String description;

        enum type
        {
            /// Integer argument, need command=5 or similar
            Integer,
            /// Boolean argument, need command=true or command=false
            Boolean,
            /// Floating point, need command=1.4 or similar
            Float,
            /// Boolean, if the argument is there it is true, if not it is false
            Flag,
        }
        public type myType;

        /// Has default argument
        public boolean isOptional;

        ///I am just going to use a double value, and cast to int or bool if need be
        public double defaultValue;

        Argument(String command, String description,type type,boolean isOptional, double defaultValue)
        {
            this.command=command;
            this.description=description;
            this.defaultValue=defaultValue;
            this.myType=type;
            this.isOptional=isOptional;
        }
    }

    List<Argument> args;
    public CommandlineCommand(String c, String d,List<Argument> argumentList)
    {
        command=c.trim();
        description=d;
        args=argumentList;
    }

    final static Attribute[] command_colors = {Attribute.RED_BACK(),Attribute.GREEN_BACK(),Attribute.BLUE_BACK(),Attribute.YELLOW_BACK()};
    public void print(int commandI)
    {
        int MaxNameLength =0;
        for (var a : args)
        {
            MaxNameLength=Math.max(MaxNameLength,a.command.length());
        }



        //First print command
        System.out.print(colorize("###",Attribute.BOLD(),Attribute.BLUE_TEXT(),Attribute.BLUE_BACK())+colorize(" ",command_colors[commandI])+" "+ colorize(command,Attribute.ITALIC(),Attribute.BRIGHT_WHITE_TEXT(),Attribute.BOLD()));
        //Then all arguments

        Attribute[] colors = {Attribute.RED_TEXT(),Attribute.GREEN_TEXT(),Attribute.BLUE_TEXT(),Attribute.YELLOW_TEXT()};
        int argi=0;
        for (var a : args)
        {
            System.out.print(colorize( " "+a.command,colors[argi], a.isOptional?Attribute.NONE():Attribute.UNDERLINE(),Attribute.ITALIC(),Attribute.BOLD() ));
            switch (a.myType)
            {
                case Flag -> System.out.print(colorize(" ",colors[argi],Attribute.ITALIC(), a.isOptional?Attribute.NONE():Attribute.UNDERLINE()));//Just a flag, no need to include anything
                case Float -> System.out.print(colorize(" "+(a.isOptional?a.defaultValue:"Float"),Attribute.ITALIC(),Attribute.BOLD() , a.isOptional?Attribute.NONE():Attribute.UNDERLINE(),colors[argi]));//Just a flag, no need to include anything
                case Integer -> System.out.print(colorize(" "+(a.isOptional?(int)a.defaultValue:"Integer "),Attribute.ITALIC(),Attribute.BOLD() , a.isOptional?Attribute.NONE():Attribute.UNDERLINE(),colors[argi]));//Just a flag, no need to include anything
                case Boolean -> System.out.print(colorize(" "+(a.isOptional?(int)a.defaultValue:"True/False "),Attribute.ITALIC(),Attribute.BOLD() , a.isOptional?Attribute.NONE():Attribute.UNDERLINE(),colors[argi]));//Just a flag, no need to include anything
            }
            argi=(argi+1)%colors.length;
        }
        //Finish line
        System.out.println();
        //Now print description
        System.out.println(colorize("###",Attribute.BOLD(),Attribute.BLUE_TEXT(),Attribute.BLUE_BACK())+colorize(" ",command_colors[commandI])+"\t\t"+ colorize(description,Attribute.ITALIC(),Attribute.BRIGHT_WHITE_TEXT()));
        if (!args.isEmpty())
        {
            argi=0;
            System.out.println(colorize("###",Attribute.BOLD(),Attribute.BLUE_TEXT(),Attribute.BLUE_BACK())+colorize(" ",command_colors[commandI])+colorize("\t\tArguments:",Attribute.WHITE_TEXT()));
            for (var a : args) {
                System.out.println(colorize("###",Attribute.BOLD(),Attribute.BLUE_TEXT(),Attribute.BLUE_BACK())+colorize(" ",command_colors[commandI])+"\t\t\t"+colorize( " "+a.command+org.apache.commons.lang3.StringUtils.repeat(' ',MaxNameLength-a.command.length())+" "+(a.isOptional?"(optional) ":"           ")+(a.myType)+org.apache.commons.lang3.StringUtils.repeat(' ',7-a.myType.toString().length())+": "+a.description+" ",colors[argi], a.isOptional?Attribute.NONE():Attribute.UNDERLINE(),Attribute.ITALIC(),Attribute.BOLD() ));
                argi=(argi+1)%colors.length;
            }
        }
        System.out.println(colorize("###",Attribute.BOLD(),Attribute.BLUE_TEXT(),Attribute.BLUE_BACK())+colorize(" ",command_colors[commandI]));
        ++commandI;
    }

    /// Check if the strings match the command
    /// @return  returns a list of arguments as doubles (1.0 or 0.0 if boolean, and integer if ints), returns null if not matching
    public double[] match(List<String> strings)
    {
        if (strings.isEmpty())
            return null;

        if (strings.getFirst().equalsIgnoreCase(command))
        {
            //Check if any argument mis-matches
            double[] out = new double[args.size()];
            for (int i = 0; i< args.size(); ++i)
            {
                //Index in strings
                int j;
                for (j = 0; j < strings.size();++j)
                {
                    if (strings.get(j).equalsIgnoreCase(args.get(i).command))
                    {
                        break;
                    }
                }

                //Not found, check if it is optional
                if (j==strings.size())
                {
                    if (args.get(i).isOptional)
                    {
                        out[i]=args.get(i).defaultValue;
                    }
                    else
                        return null;
                }
                else
                {
                    //Got it, if this is a flag, just set it
                    if (args.get(i).myType== Argument.type.Flag)
                        out[i]=1.0;
                    else
                    {
                        //If this is strings[j] value, read value
                        if (j+1< strings.size())
                        {
                            try
                            {
                                //Look for true or false
                                if (args.get(i).myType== Argument.type.Boolean)
                                {
                                    if (strings.get(j+1).equalsIgnoreCase("true"))
                                    {
                                        out[i]=1.0;
                                    } else if (strings.get(j+1).equalsIgnoreCase("false"))
                                    {
                                        out[i]=0.0;
                                    } else
                                    {
                                        out[i]=Double.parseDouble(strings.get(j+1))>0.5?1.0:0.0;
                                    }

                                }
                                else
                                    out[i]=Double.parseDouble(strings.get(j+1));
                            }
                            //oh whatever
                            catch (Exception e)
                            {
                                return null;
                            }

                        }
                        else
                        {
                            //Oh well, go with default value
                            if (args.get(i).isOptional)
                            {
                                out[i]=args.get(i).defaultValue;
                            }
                            else
                                return null;
                        }
                    }
                }
            }

            //Match, may be {} if there were no arguments
            return out;
        }
        else
            return null;
    }
};
